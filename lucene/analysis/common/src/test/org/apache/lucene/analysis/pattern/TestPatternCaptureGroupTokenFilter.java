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
import java.io.StringReader;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.MockTokenizer;
import org.apache.lucene.analysis.TokenStream;

/**
 * no pattern no match true no match false no match [] no capture true no
 * capture false capture start true capture start false capture middle true
 * capture middle false capture end true capture end false capture start middle
 * true capture start middle false capture start end true capture start end
 * false capture middle end true capture middle end false capture start middle
 * end true capture start middle end false
 *
 * multi tokens?
 *
 *
 */
public class TestPatternCaptureGroupTokenFilter extends BaseTokenStreamTestCase {

  public void testNoPattern() throws Exception {
    testPatterns(
        "foobarbaz",
        new String[] {},
        new String[] {"foobarbaz"},
        new int[] {0},
        new int[] {9},
        new int[] {1},
        false
    );
    testPatterns(
        "foobarbaz",
        new String[] {},
        new String[] {"foobarbaz"},
        new int[] {0},
        new int[] {9},
        new int[] {1},
        true
    );

    testPatterns(
        "foo bar baz",
        new String[] {},
        new String[] {"foo","bar","baz"},
        new int[] {0,4,8},
        new int[] {3,7,11},
        new int[] {1,1,1},
        false
    );

    testPatterns(
        "foo bar baz",
        new String[] {},
        new String[] {"foo","bar","baz"},
        new int[] {0,4,8},
        new int[] {3,7,11},
        new int[] {1,1,1},
        true
    );
  }

  public void testNoMatch() throws Exception {
    testPatterns(
        "foobarbaz",
        new String[] {"xx"},
        new String[] {"foobarbaz"},
        new int[] {0},
        new int[] {9},
        new int[] {1},
        false
    );
    testPatterns(
        "foobarbaz",
        new String[] {"xx"},
        new String[] {"foobarbaz"},
        new int[] {0},
        new int[] {9},
        new int[] {1},
        true
    );

    testPatterns(
        "foo bar baz",
        new String[] {"xx"},
        new String[] {"foo","bar","baz"},
        new int[] {0,4,8},
        new int[] {3,7,11},
        new int[] {1,1,1},
        false
    );

    testPatterns(
        "foo bar baz",
        new String[] {"xx"},
        new String[] {"foo","bar","baz"},
        new int[] {0,4,8},
        new int[] {3,7,11},
        new int[] {1,1,1},
        true
    );
  }

  public void testNoCapture() throws Exception {
    testPatterns(
        "foobarbaz",
        new String[] {".."},
        new String[] {"foobarbaz"},
        new int[] {0},
        new int[] {9},
        new int[] {1},
        false
    );
    testPatterns(
        "foobarbaz",
        new String[] {".."},
        new String[] {"foobarbaz"},
        new int[] {0},
        new int[] {9},
        new int[] {1},
        true
    );

    testPatterns(
        "foo bar baz",
        new String[] {".."},
        new String[] {"foo","bar","baz"},
        new int[] {0,4,8},
        new int[] {3,7,11},
        new int[] {1,1,1},
        false
    );

    testPatterns(
        "foo bar baz",
        new String[] {".."},
        new String[] {"foo","bar","baz"},
        new int[] {0,4,8},
        new int[] {3,7,11},
        new int[] {1,1,1},
        true
    );
  }

  public void testCaptureAll() throws Exception {
    testPatterns(
        "foobarbaz",
        new String[] {"(.+)"},
        new String[] {"foobarbaz"},
        new int[] {0},
        new int[] {9},
        new int[] {1},
        false
    );
    testPatterns(
        "foobarbaz",
        new String[] {"(.+)"},
        new String[] {"foobarbaz"},
        new int[] {0},
        new int[] {9},
        new int[] {1},
        true
    );

    testPatterns(
        "foo bar baz",
        new String[] {"(.+)"},
        new String[] {"foo","bar","baz"},
        new int[] {0,4,8},
        new int[] {3,7,11},
        new int[] {1,1,1},
        false
    );

    testPatterns(
        "foo bar baz",
        new String[] {"(.+)"},
        new String[] {"foo","bar","baz"},
        new int[] {0,4,8},
        new int[] {3,7,11},
        new int[] {1,1,1},
        true
    );
  }

  public void testCaptureStart() throws Exception {
    testPatterns(
        "foobarbaz",
        new String[] {"^(.)"},
        new String[] {"f"},
        new int[] {0},
        new int[] {1},
        new int[] {1},
        false
    );
    testPatterns(
        "foobarbaz",
        new String[] {"^(.)"},
        new String[] {"foobarbaz","f"},
        new int[] {0,0},
        new int[] {9,1},
        new int[] {1,0},
        true
    );

    testPatterns(
        "foo bar baz",
        new String[] {"^(.)"},
        new String[] {"f","b","b"},
        new int[] {0,4,8},
        new int[] {1,5,9},
        new int[] {1,1,1},
        false
    );

    testPatterns(
        "foo bar baz",
        new String[] {"^(.)"},
        new String[] {"foo","f","bar","b","baz","b"},
        new int[] {0,0,4,4,8,8},
        new int[] {3,1,7,5,11,9},
        new int[] {1,0,1,0,1,0},
        true
    );
  }

  public void testCaptureMiddle() throws Exception {
    testPatterns(
        "foobarbaz",
        new String[] {"^.(.)."},
        new String[] {"o"},
        new int[] {1},
        new int[] {2},
        new int[] {1},
        false
    );
    testPatterns(
        "foobarbaz",
        new String[] {"^.(.)."},
        new String[] {"foobarbaz","o"},
        new int[] {0,1},
        new int[] {9,2},
        new int[] {1,0},
        true
    );

    testPatterns(
        "foo bar baz",
        new String[] {"^.(.)."},
        new String[] {"o","a","a"},
        new int[] {1,5,9},
        new int[] {2,6,10},
        new int[] {1,1,1},
        false
    );

    testPatterns(
        "foo bar baz",
        new String[] {"^.(.)."},
        new String[] {"foo","o","bar","a","baz","a"},
        new int[] {0,1,4,5,8,9},
        new int[] {3,2,7,6,11,10},
        new int[] {1,0,1,0,1,0},
        true
    );
  }

  public void testCaptureEnd() throws Exception {
    testPatterns(
        "foobarbaz",
        new String[] {"(.)$"},
        new String[] {"z"},
        new int[] {8},
        new int[] {9},
        new int[] {1},
        false
    );
    testPatterns(
        "foobarbaz",
        new String[] {"(.)$"},
        new String[] {"foobarbaz","z"},
        new int[] {0,8},
        new int[] {9,9},
        new int[] {1,0},
        true
    );

    testPatterns(
        "foo bar baz",
        new String[] {"(.)$"},
        new String[] {"o","r","z"},
        new int[] {2,6,10},
        new int[] {3,7,11},
        new int[] {1,1,1},
        false
    );

    testPatterns(
        "foo bar baz",
        new String[] {"(.)$"},
        new String[] {"foo","o","bar","r","baz","z"},
        new int[] {0,2,4,6,8,10},
        new int[] {3,3,7,7,11,11},
        new int[] {1,0,1,0,1,0},
        true
    );
  }

  public void testCaptureStartMiddle() throws Exception {
    testPatterns(
        "foobarbaz",
        new String[] {"^(.)(.)"},
        new String[] {"f","o"},
        new int[] {0,1},
        new int[] {1,2},
        new int[] {1,0},
        false
    );
    testPatterns(
        "foobarbaz",
        new String[] {"^(.)(.)"},
        new String[] {"foobarbaz","f","o"},
        new int[] {0,0,1},
        new int[] {9,1,2},
        new int[] {1,0,0},
        true
    );

    testPatterns(
        "foo bar baz",
        new String[] {"^(.)(.)"},
        new String[] {"f","o","b","a","b","a"},
        new int[] {0,1,4,5,8,9},
        new int[] {1,2,5,6,9,10},
        new int[] {1,0,1,0,1,0},
        false
    );

    testPatterns(
        "foo bar baz",
        new String[] {"^(.)(.)"},
        new String[] {"foo","f","o","bar","b","a","baz","b","a"},
        new int[] {0,0,1,4,4,5,8,8,9},
        new int[] {3,1,2,7,5,6,11,9,10},
        new int[] {1,0,0,1,0,0,1,0,0},
        true
    );
  }

  public void testCaptureStartEnd() throws Exception {
    testPatterns(
        "foobarbaz",
        new String[] {"^(.).+(.)$"},
        new String[] {"f","z"},
        new int[] {0,8},
        new int[] {1,9},
        new int[] {1,0},
        false
    );
    testPatterns(
        "foobarbaz",
        new String[] {"^(.).+(.)$"},
        new String[] {"foobarbaz","f","z"},
        new int[] {0,0,8},
        new int[] {9,1,9},
        new int[] {1,0,0},
        true
    );

    testPatterns(
        "foo bar baz",
        new String[] {"^(.).+(.)$"},
        new String[] {"f","o","b","r","b","z"},
        new int[] {0,2,4,6,8,10},
        new int[] {1,3,5,7,9,11},
        new int[] {1,0,1,0,1,0},
        false
    );

    testPatterns(
        "foo bar baz",
        new String[] {"^(.).+(.)$"},
        new String[] {"foo","f","o","bar","b","r","baz","b","z"},
        new int[] {0,0,2,4,4,6,8,8,10},
        new int[] {3,1,3,7,5,7,11,9,11},
        new int[] {1,0,0,1,0,0,1,0,0},
        true
    );
  }

  public void testCaptureMiddleEnd() throws Exception {
    testPatterns(
        "foobarbaz",
        new String[] {"(.)(.)$"},
        new String[] {"a","z"},
        new int[] {7,8},
        new int[] {8,9},
        new int[] {1,0},
        false
    );
    testPatterns(
        "foobarbaz",
        new String[] {"(.)(.)$"},
        new String[] {"foobarbaz","a","z"},
        new int[] {0,7,8},
        new int[] {9,8,9},
        new int[] {1,0,0},
        true
    );

    testPatterns(
        "foo bar baz",
        new String[] {"(.)(.)$"},
        new String[] {"o","o","a","r","a","z"},
        new int[] {1,2,5,6,9,10},
        new int[] {2,3,6,7,10,11},
        new int[] {1,0,1,0,1,0},
        false
    );

    testPatterns(
        "foo bar baz",
        new String[] {"(.)(.)$"},
        new String[] {"foo","o","o","bar","a","r","baz","a","z"},
        new int[] {0,1,2,4,5,6,8,9,10},
        new int[] {3,2,3,7,6,7,11,10,11},
        new int[] {1,0,0,1,0,0,1,0,0},
        true
    );
  }

  public void testMultiCaptureOverlap() throws Exception {
    testPatterns(
        "foobarbaz",
        new String[] {"(.(.(.)))"},
        new String[] {"foo","oo","o","bar","ar","r","baz","az","z"},
        new int[] {0,1,2,3,4,5,6,7,8},
        new int[] {3,3,3,6,6,6,9,9,9},
        new int[] {1,0,0,0,0,0,0,0,0},
        false
    );
    testPatterns(
        "foobarbaz",
        new String[] {"(.(.(.)))"},
        new String[] {"foobarbaz","foo","oo","o","bar","ar","r","baz","az","z"},
        new int[] {0,0,1,2,3,4,5,6,7,8},
        new int[] {9,3,3,3,6,6,6,9,9,9},
        new int[] {1,0,0,0,0,0,0,0,0,0},
        true
    );

    testPatterns(
        "foo bar baz",
        new String[] {"(.(.(.)))"},
        new String[] {"foo","oo","o","bar","ar","r","baz","az","z"},
        new int[] {0,1,2,4,5,6,8,9,10},
        new int[] {3,3,3,7,7,7,11,11,11},
        new int[] {1,0,0,1,0,0,1,0,0},
        false
    );

    testPatterns(
        "foo bar baz",
        new String[] {"(.(.(.)))"},
        new String[] {"foo","oo","o","bar","ar","r","baz","az","z"},
        new int[] {0,1,2,4,5,6,8,9,10},
        new int[] {3,3,3,7,7,7,11,11,11},
        new int[] {1,0,0,1,0,0,1,0,0},
        true
    );
  }

  public void testMultiPattern() throws Exception {
    testPatterns(
        "aaabbbaaa",
        new String[] {"(aaa)","(bbb)","(ccc)"},
        new String[] {"aaa","aaa","bbb"},
        new int[] {0,6,3},
        new int[] {3,9,6},
        new int[] {1,0,0},
        false
    );
    testPatterns(
        "aaabbbaaa",
        new String[] {"(aaa)","(bbb)","(ccc)"},
        new String[] {"aaabbbaaa","aaa","aaa","bbb"},
        new int[] {0,0,6,3},
        new int[] {9,3,9,6},
        new int[] {1,0,0,0},
        true
    );

    testPatterns(
        "aaa bbb aaa",
        new String[] {"(aaa)","(bbb)","(ccc)"},
        new String[] {"aaa","bbb","aaa"},
        new int[] {0,4,8},
        new int[] {3,7,10},
        new int[] {1,0,0},
        false
    );

    testPatterns(
        "aaa bbb aaa",
        new String[] {"(aaa)","(bbb)","(ccc)"},
        new String[] {"aaa","bbb","aaa"},
        new int[] {0,4,8},
        new int[] {3,7,10},
        new int[] {1,0,0},
        true
    );
  }

  private void testPatterns(String input, String[] regexes, String[] tokens,
      int[] startOffsets, int[] endOffsets, int[] positions,
      boolean preserveOriginal) throws Exception {
    Pattern[] patterns = new Pattern[regexes.length];
    for (int i = 0; i < regexes.length; i++) {
      patterns[i] = Pattern.compile(regexes[i]);
    }
    TokenStream ts = new PatternCaptureGroupTokenFilter(new MockTokenizer(
        new StringReader(input), MockTokenizer.WHITESPACE, false), patterns,
        preserveOriginal);
    assertTokenStreamContents(ts, tokens, startOffsets, endOffsets, positions);
  }

}
