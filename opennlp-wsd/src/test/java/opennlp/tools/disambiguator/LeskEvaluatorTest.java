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

import java.util.ArrayList;
import java.util.List;

import opennlp.tools.disambiguator.datareader.SensevalReader;
import opennlp.tools.disambiguator.ims.WTDIMS;
import opennlp.tools.disambiguator.lesk.Lesk;
import opennlp.tools.disambiguator.lesk.LeskParameters;

import org.junit.Test;

public class LeskEvaluatorTest {

  static SensevalReader seReader = new SensevalReader();

  @Test
  public static void main(String[] args) {
    WSDHelper.print("Evaluation Started");
    String modelsDir = "src\\test\\resources\\models\\";
    WSDHelper.loadTokenizer(modelsDir + "en-token.bin");
    WSDHelper.loadLemmatizer(modelsDir + "en-lemmatizer.dict");
    WSDHelper.loadTagger(modelsDir + "en-pos-maxent.bin");
    Lesk lesk = new Lesk();
    LeskParameters leskParams = new LeskParameters();
    boolean a[] = { true, true, true, true, true, false, false, false, false,
        false };
    leskParams.setFeatures(a);
    leskParams.setLeskType(LeskParameters.LESK_TYPE.LESK_EXT_CTXT);
    lesk.setParams(leskParams);

    ArrayList<String> words = seReader.getSensevalWords();

    for (String word : words) {
      WSDEvaluator evaluator = new WSDEvaluator(lesk);

      // don't take verbs because they are not from WordNet
      if (!word.split("\\.")[1].equals("v")) {

        ArrayList<WSDSample> instances = getTestData(word);
        if (instances != null) {
          WSDHelper.print("------------------" + word + "------------------");
          for (WSDSample instance : instances) {
            if (instance.getSenseIDs() != null
                && !instance.getSenseIDs().get(0).equals("null")) {
              evaluator.evaluateSample(instance);
            }
          }
          WSDHelper.print(evaluator.toString());
        } else {
          WSDHelper.print("null instances");
        }
      }
    }
  }

  protected static ArrayList<WSDSample> getTestData(String wordTag) {

    ArrayList<WSDSample> instances = new ArrayList<WSDSample>();
    for (WordToDisambiguate wtd : seReader.getSensevalData(wordTag)) {
      List<WordPOS> words = WSDHelper.getAllRelevantWords(wtd);
      int targetWordIndex = 0;
      for (int i = 0; i < words.size(); i++) {
        if (words.get(i).isTarget) {
          targetWordIndex = i;
        }
      }
      String[] tags = new String[words.size()];
      String[] tokens = new String[words.size()];
      for (int i = 0; i < words.size(); i++) {
        tags[i] = words.get(i).getPosTag();
        tokens[i] = words.get(i).getWord();
      }
      String targetLemma = WSDHelper.getLemmatizer().lemmatize(
          tokens[targetWordIndex], tags[targetWordIndex]);

      WSDSample sample = new WSDSample(tokens, tags, targetWordIndex,
          targetLemma);
      sample.setSenseIDs(wtd.getSenseIDs());
      if (sample != null) {
        if (sample.getSenseIDs().get(0) != null
            && !sample.getSenseIDs().get(0).equalsIgnoreCase("U")) {
          instances.add(sample);
        }
      }
    }
    return instances;
  }

}
