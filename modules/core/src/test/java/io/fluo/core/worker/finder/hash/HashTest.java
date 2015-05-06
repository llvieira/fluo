/*
 * Copyright 2014 Fluo authors (see AUTHORS)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */


package io.fluo.core.worker.finder.hash;

import java.util.Random;

import com.google.common.math.DoubleMath;
import io.fluo.accumulo.iterators.NotificationHashFilter;
import io.fluo.accumulo.util.ColumnConstants;
import io.fluo.api.data.Bytes;
import io.fluo.api.data.Column;
import io.fluo.core.impl.Notification;
import io.fluo.core.util.ColumnUtil;
import org.apache.accumulo.core.data.Key;
import org.junit.Assert;
import org.junit.Test;

public class HashTest {

  @Test
  public void testHashingConsistency() {

    Random rand = new Random(7);

    int count = 0;

    for (int i = 0; i < 1000; i++) {
      byte[] row = new byte[16];
      byte[] cf = new byte[16];
      byte[] cq = new byte[16];

      rand.nextBytes(row);
      rand.nextBytes(cf);
      rand.nextBytes(cq);

      if (check(row, cf, cq)) {
        count++;
      }
    }

    double percentage = count / 1000.0;
    double expected = 1.0 / 7;

    Assert.assertTrue(DoubleMath.fuzzyEquals(percentage, expected, .1));
  }

  private boolean check(byte[] row, byte[] cf, byte[] cq) {
    Column col = new Column(Bytes.of(cf), Bytes.of(cq));

    byte[] cfcq = ColumnUtil.concatCFCQ(col);
    Key k = new Key(row, ColumnConstants.NOTIFY_CF.toArray(), cfcq, new byte[0], 6);
    boolean accept = NotificationHashFilter.accept(k, 7, 3);
    Assert.assertEquals(accept, HashNotificationFinder.shouldProcess(Notification.from(k), 7, 3));
    return accept;
  }
}
