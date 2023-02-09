/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package opennlp.tools.disambiguator;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import opennlp.tools.disambiguator.datareader.SemcorReaderExtended;
import opennlp.tools.lemmatizer.Lemmatizer;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.Span;
import opennlp.tools.util.TrainingParameters;

/**
 * This is the test class for {@link WSDisambiguatorME}.
 * <p/>
 * The scope of this test is to make sure that the WSDisambiguatorME code can be
 * executed. This test can not detect mistakes which lead to incorrect feature
 * generation or other mistakes which decrease the disambiguation performance of
 * the disambiguator.
 * <p/>
 * In this test the {@link WSDisambiguatorME} is trained with Semcor
 * and then the computed model is used to predict sentences
 * from the training sentences.
 */

class WSDTester {
  // TODO write more tests
  // TODO modify when we fix the parameter model

  static String modelsDir = "src/test/resources/models/";
  static String trainingDataDirectory = "src/test/resources/supervised/models/";

  static WSDDefaultParameters params;
  static WSDisambiguatorME wsdME;
  static WSDModel model;

  static String test = "please.v";
  static File outFile;

  static String test1 = "We need to discuss an important topic, please write to me soon.";
  static String test2 = "The component was highly radioactive to the point that"
      + " it has been activated the second it touched water";
  static String test3 = "The summer is almost over and I did not go to the beach even once";

  static String[] sentence1;
  static String[] sentence2;
  static String[] sentence3;

  static String[] tags1;
  static String[] tags2;
  static String[] tags3;

  static List<List<String>> lemmas1;
  static List<List<String>> lemmas2;
  static List<List<String>> lemmas3;

  /*
   * Setup the testing variables
   */
  @BeforeAll
  static void setUpAndTraining() {
    WSDHelper.loadTokenizer(modelsDir + "en-token.bin");
    WSDHelper.loadTagger(modelsDir + "en-pos-maxent.bin");
    WSDHelper.loadLemmatizer(modelsDir + "en-lemmatizer.dict.gz");

    sentence1 = WSDHelper.getTokenizer().tokenize(test1);
    sentence2 = WSDHelper.getTokenizer().tokenize(test2);
    sentence3 = WSDHelper.getTokenizer().tokenize(test3);

    tags1 = WSDHelper.getTagger().tag(sentence1);
    tags2 = WSDHelper.getTagger().tag(sentence2);
    tags3 = WSDHelper.getTagger().tag(sentence3);

    final Lemmatizer lemmatizer = WSDHelper.getLemmatizer();
    lemmas1 = lemmatizer.lemmatize(Arrays.asList(sentence1), Arrays.asList(tags1));
    lemmas2 = lemmatizer.lemmatize(Arrays.asList(sentence2), Arrays.asList(tags2));
    lemmas3 = lemmatizer.lemmatize(Arrays.asList(sentence3), Arrays.asList(tags3));

    params = new WSDDefaultParameters("");
    params.setTrainingDataDirectory(trainingDataDirectory);
    TrainingParameters trainingParams = new TrainingParameters();
    SemcorReaderExtended sr = new SemcorReaderExtended();
    ObjectStream<WSDSample> sampleStream = sr.getSemcorDataStream(test);

    WSDModel writeModel = null;
    /*
     * Tests training the disambiguator We test both writing and reading a model
     * file trained by semcor
     */

    try {
      writeModel = WSDisambiguatorME
          .train("en", sampleStream, trainingParams, params);
      assertNotNull(writeModel, "Checking the model to be written");
      writeModel.writeModel(params.getTrainingDataDirectory() + test);
      outFile = new File(
          params.getTrainingDataDirectory() + test + ".wsd.model");
      model = new WSDModel(outFile);
      assertNotNull(model, "Checking the read model");
      wsdME = new WSDisambiguatorME(model, params);
      assertNotNull(wsdME, "Checking the disambiguator");
    } catch (IOException e1) {
      e1.printStackTrace();
      fail("Exception in training: " + e1.getMessage());
    }
  }

  /*
   * Tests disambiguating only one word : The ambiguous word "please"
   */
  @Test
  void testOneWordDisambiguation() {
    String sense = wsdME.disambiguate(sentence1, tags1, lemmas1.get(0).toArray(new String[0]), 8);
    assertEquals("WORDNET please%2:37:00::", sense, "Check 'please' sense ID");
  }

  /*
   * Tests disambiguating a word Span In this case we test a mix of monosemous
   * and polysemous words as well as words that do not need disambiguation such
   * as determiners
   */
  @Test
  void testWordSpanDisambiguation() {
    Span span = new Span(3, 7);
    List<String> senses = wsdME.disambiguate(sentence2, tags2, lemmas2.get(0).toArray(new String[0]), span);

    assertEquals(5, senses.size(), "Check number of returned words");
    assertEquals("WORDNET highly%4:02:01::",
        senses.get(0), "Check 'highly' sense ID");
    assertEquals(
        "WORDNET radioactive%3:00:00::", senses.get(1), "Check 'radioactive' sense ID");
    assertEquals("WSDHELPER to", senses.get(2), "Check preposition");
    assertEquals("WSDHELPER determiner", senses.get(3), "Check determiner");
  }

  /*
   * Tests disambiguating all the words
   */
  @Test
  void testAllWordsDisambiguation() {
    List<String> senses = wsdME.disambiguate(sentence3, tags3, lemmas3.get(0).toArray(new String[0]));

    assertEquals(15, senses.size(), "Check number of returned words");
    assertEquals("WSDHELPER personal pronoun",
        senses.get(6), "Check preposition");
  }

}
