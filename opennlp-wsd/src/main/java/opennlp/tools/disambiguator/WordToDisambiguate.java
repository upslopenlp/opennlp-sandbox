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
import java.util.Arrays;

import net.sf.extjwnl.data.POS;

public class WordToDisambiguate {

  // TODO Check if it is necessary to add an attribute [word] since the word in
  // the sentence is not necessarily in the base form ??

  protected String[] sentence;
  protected String[] posTags;

  protected int wordIndex;

  protected int sense;

  protected ArrayList<String> senseIDs;

  public WordToDisambiguate(String[] sentence, int wordIndex)
      throws IllegalArgumentException {
    super();

    if (wordIndex > sentence.length) {
      throw new IllegalArgumentException("The index is out of bounds !");
    }

    this.sentence = sentence;
    this.posTags = WSDHelper.getTagger().tag(sentence);

    this.wordIndex = wordIndex;

    this.sense = -1;
  }

  public WordToDisambiguate(String[] sentence, int wordIndex, int sense)
      throws IllegalArgumentException {
    super();

    if (wordIndex > sentence.length) {
      throw new IllegalArgumentException("The index is out of bounds !");
    }

    this.sentence = sentence;
    this.posTags = WSDHelper.getTagger().tag(sentence);

    this.wordIndex = wordIndex;

    this.sense = sense;
  }

  public WordToDisambiguate(String[] sentence, int wordIndex,
      ArrayList<String> senseIDs) throws IllegalArgumentException {
    super();

    if (wordIndex > sentence.length) {
      throw new IllegalArgumentException("The index is out of bounds !");
    }

    this.sentence = sentence;
    this.posTags = WSDHelper.getTagger().tag(sentence);

    this.wordIndex = wordIndex;

    this.senseIDs = senseIDs;
  }

  public WordToDisambiguate(String[] sentence, String[] tokenTags, int wordIndex) {
    this(sentence, wordIndex, -1);
  }

  public WordToDisambiguate() {
    String[] emptyString = {};
    int emptyInteger = 0;

    this.sentence = emptyString;
    this.wordIndex = emptyInteger;
    this.sense = -1;

  }

  // Sentence
  public String[] getSentence() {
    return sentence;
  }

  public void setSentence(String[] sentence) {
    this.sentence = sentence;
  }

  // Sentence Pos-Tags
  public String[] getPosTags() {
    return posTags;
  }

  public void setPosTags(String[] posTags) {
    this.posTags = posTags;
  }

  // Word to disambiguate
  public int getWordIndex() {
    return wordIndex;
  }

  public String getRawWord() {

    String wordBaseForm = WSDHelper.getLemmatizer().lemmatize(
        this.sentence[wordIndex], this.posTags[wordIndex]);

    String ref = "";

    if ((WSDHelper.getPOS(this.posTags[wordIndex]) != null)) {
      if (WSDHelper.getPOS(this.posTags[wordIndex]).equals(POS.VERB)) {
        ref = wordBaseForm + ".v";
      } else if (WSDHelper.getPOS(this.posTags[wordIndex]).equals(POS.NOUN)) {
        ref = wordBaseForm + ".n";
      } else if (WSDHelper.getPOS(this.posTags[wordIndex])
          .equals(POS.ADJECTIVE)) {
        ref = wordBaseForm + ".a";
      } else if (WSDHelper.getPOS(this.posTags[wordIndex]).equals(POS.ADVERB)) {
        ref = wordBaseForm + ".r";
      }

    }

    return ref;
  }

  public String getWord() {
    return this.sentence[this.wordIndex];
  }

  public String getPosTag() {
    return this.posTags[this.wordIndex];
  }

  public void setWordIndex(int wordIndex) {
    this.wordIndex = wordIndex;
  }

  // Word to disambiguate sense
  public int getSense() {
    return sense;
  }

  public void setSense(int sense) {
    this.sense = sense;
  }

  // Sense as in the source
  // TODO fix the conflict between this ID of the sense and that in the
  // attribute [sense]
  public ArrayList<String> getSenseIDs() {
    return senseIDs;
  }

  public void setSenseIDs(ArrayList<String> senseIDs) {
    this.senseIDs = senseIDs;
  }

  public String toString() {
    return (wordIndex + "\t" + getWord() + "\n" + sentence);
  }

  public void print() {
    WSDHelper.print("Sentence:  " + Arrays.asList(sentence) + "\n" + "Index: "
        + wordIndex + "\n" + "Word: " + getWord() + "\n" + "Sense ID: "
        + senseIDs.get(0));
  }
}
