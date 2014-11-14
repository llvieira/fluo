/*
 * Copyright 2014 Fluo authors (see AUTHORS)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.fluo.stress.trie;

import java.io.File;
import java.util.TreeSet;

import com.google.common.base.Strings;
import io.fluo.api.config.FluoConfiguration;
import io.fluo.core.util.AccumuloUtil;
import org.apache.accumulo.core.client.Connector;
import org.apache.hadoop.io.Text;

public class Split {

  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      System.err.println("Usage: " + Split.class.getSimpleName() + " <fluo props> <num tablets>");
      System.exit(-1);
    }

    FluoConfiguration config = new FluoConfiguration(new File(args[0]));
    int numTablets = Integer.parseInt(args[1]);

    TreeSet<Text> splits = genSplits(numTablets);
    addSplits(config, splits);
  }

  private static TreeSet<Text> genSplits(int numTablets) {

    TreeSet<Text> splits = new TreeSet<>();

    int numSplits = numTablets - 1;
    int distance = (((int)Math.pow(Character.MAX_RADIX, Node.HASH_LEN)-1) / numTablets) + 1;
    int split = distance;
    for (int i = 0; i < numSplits; i++) {
      splits.add(new Text(Strings.padStart(Integer.toString(split, Character.MAX_RADIX), Node.HASH_LEN, '0')));
      split += distance;
    }

    return splits;
  }

  private static void addSplits(FluoConfiguration config, TreeSet<Text> splits) throws Exception {
    Connector conn = AccumuloUtil.getConnector(config);
    conn.tableOperations().addSplits(config.getAccumuloTable(), splits);
  }
}