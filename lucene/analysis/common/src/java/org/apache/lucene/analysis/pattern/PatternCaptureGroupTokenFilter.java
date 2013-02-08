package org.apache.lucene.analysis.pattern;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.CharsRef;

/**
 * CaptureGroup uses Java regexes to emit multiple tokens - one for each capture
 * group in one or more patterns.
 *
 * <p>For example, a pattern like:</p>
 *
 * <p><code>"(https?://([a-zA-Z\-_0-9.]+))"</code></p>
 *
 * <p>when matched against the string "http://www.foo.com/index" would return
 * the tokens "https://www.foo.com" and "www.foo.com".</p>
 *
 * <p>If none of the patterns match, or if preserveOriginal is true, the original
 * token will be preserved.</p>
 * <p>A camelCaseFilter could be written as:</p>
 * <p><code>
 *   "(?:^|\b)([a-z]+)"          # returns "camel"           <br />
 *   "(?<=[a-z])([A-Z][a-z]+)"   # returns "Camel", "Filter" <br />
 * </code>
 * </p>
 * <p>
 * plus if {@link #preserveOriginal} is true, it would also return
 * <code>"camelCaseFilter</code>
 * </p>
 */
public final class PatternCaptureGroupTokenFilter extends TokenFilter {

  private final CharTermAttribute charTermAttr = addAttribute(CharTermAttribute.class);
  private final PositionIncrementAttribute posAttr = addAttribute(PositionIncrementAttribute.class);
  private final OffsetAttribute offsetAttr = addAttribute(OffsetAttribute.class);
  private final Matcher[] matchers;
  private final CharsRef spare = new CharsRef();
  private int groupCount = -1;
  private int currentGroup = -1;
  private int currentMatcher = -1;
  private Matcher matcher;
  private int charOffsetStart;
  private boolean preserveOriginal;
  private boolean originalPreserved = false;

  /**
   * @param input
   *          the input {@link TokenStream}
   * @param patterns
   *          an array of {@link Pattern} objects to match against each token
   * @param preserveOriginal
   *          set to true to return the original token even if one of the
   *          patterns matches
   */

  public PatternCaptureGroupTokenFilter(TokenStream input, Pattern[] patterns,
      boolean preserveOriginal) {
    super(input);
    this.preserveOriginal = preserveOriginal;
    this.matchers = new Matcher[patterns.length];
    for (int i = 0; i < patterns.length; i++) {
      this.matchers[i] = patterns[i].matcher("");
    }
  }

  private boolean nextMatch() {
    if (matcher != null) {
      if (matcher.find()) {
        return true;
      }
      matcher.reset("");
    }

    while (++currentMatcher < matchers.length) {
      matcher = matchers[currentMatcher];
      matcher.reset(spare);
      if (matcher.find()) {
        groupCount = matcher.groupCount();
        return true;
      }
      matcher.reset("");
    }

    currentMatcher = -1;
    matcher = null;
    return false;
  }

  private boolean emitToken(int start, int end) {
    if (start == 0 && currentMatcher == 0) {
      // if we start at 0 we can simply set the length and safe the copy
      charTermAttr.setLength(end);
    } else {
      charTermAttr.copyBuffer(spare.chars, start, end - start);
    }
    posAttr.setPositionIncrement(0);
    offsetAttr.setOffset(charOffsetStart + start, charOffsetStart + end);
    return true;
  }

  @Override
  public boolean incrementToken() throws IOException {

    if (groupCount != -1) {
      for (int i = currentGroup; i < groupCount + 1; i++) {
        final int start = matcher.start(i);
        final int end = matcher.end(i);
        if (start != end) {
          clearAttributes();
          currentGroup = i + 1;
          return emitToken(start, end);
        }
      }
      if (nextMatch()) {
        currentGroup = 1;
        return this.incrementToken();
      }
      groupCount = currentGroup = -1;
      originalPreserved = false;
    }

    if (!input.incrementToken()) {
      return false;
    }

    char[] buffer = charTermAttr.buffer();
    int length = charTermAttr.length();
    spare.copyChars(buffer, 0, length);

    while (nextMatch()) {
      for (int i = 1; i < groupCount + 1; i++) {
        final int start = matcher.start(i);
        final int end = matcher.end(i);
        if (start != end) {
          if (!originalPreserved && preserveOriginal
              && (start > 0 || end < length)) {
            originalPreserved = true;
            currentGroup = i;
            return true;
          }
          currentGroup = i + 1;
          charOffsetStart = offsetAttr.startOffset();
          return emitToken(start, end);
        }
      }
      groupCount = currentGroup = currentMatcher = -1;
    }
    return true;
  }

  @Override
  public void reset() throws IOException {
    super.reset();
    if (matcher != null) {
      matcher.reset("");
    }
    groupCount = -1;
    currentGroup = -1;
    originalPreserved = false;
    currentMatcher = -1;
  }

}
