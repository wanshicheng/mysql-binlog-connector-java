/*
 * Copyright 2013 Stanley Shyiko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.shyiko.mysql.binlog.jmx;

import com.github.shyiko.mysql.binlog.event.Event;
import com.github.shyiko.mysql.binlog.event.EventHeaderV4;
import com.github.shyiko.mysql.binlog.event.EventType;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.Test;

import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">Stanley Shyiko</a>
 */
@PrepareForTest(BinaryLogClientStatistics.class)
public class BinaryLogClientStatisticsTest extends PowerMockTestCase {

    @Test
    public void testInitialState() throws Exception {
        BinaryLogClientStatistics statistics = new BinaryLogClientStatistics();
        assertNull(statistics.getLastEvent());
        assertEquals(statistics.getSecondsSinceLastEvent(), 0L);
        assertEquals(statistics.getTotalNumberOfEventsSeen(), 0L);
        assertEquals(statistics.getNumberOfSkippedEvents(), 0L);
        assertEquals(statistics.getNumberOfDisconnects(), 0L);
    }

    @Test
    public void testOnEvent() throws Exception {
        BinaryLogClientStatistics statistics = new BinaryLogClientStatistics();
        assertNull(statistics.getLastEvent());
        assertEquals(statistics.getSecondsSinceLastEvent(), 0L);
        assertEquals(statistics.getTotalNumberOfEventsSeen(), 0L);
        assertEquals(statistics.getNumberOfSkippedEvents(), 0L);
        assertEquals(statistics.getNumberOfDisconnects(), 0L);
        statistics.onEvent(generateEvent(EventType.FORMAT_DESCRIPTION, 104, 1));
        assertEquals(statistics.getLastEvent(), "FORMAT_DESCRIPTION/0 from server 1");
        long futureTime = System.currentTimeMillis() + 1010;
        mockStatic(System.class);
        when(System.currentTimeMillis()).thenReturn(futureTime);
        assertEquals(statistics.getSecondsSinceLastEvent(), 1);
        verifyStatic();
        assertEquals(statistics.getTotalNumberOfEventsSeen(), 1);
        assertEquals(statistics.getNumberOfSkippedEvents(), 0);
    }

    @Test
    public void testOnEventDeserializationFailure() throws Exception {
        BinaryLogClientStatistics statistics = new BinaryLogClientStatistics();
        statistics.onEvent(generateEvent(EventType.FORMAT_DESCRIPTION, 104, 1));
        statistics.onEventDeserializationFailure(null, null);
        assertNull(statistics.getLastEvent());
        assertEquals(statistics.getTotalNumberOfEventsSeen(), 2L);
        assertEquals(statistics.getNumberOfSkippedEvents(), 1L);
    }

    @Test
    public void testOnDisconnect() throws Exception {
        BinaryLogClientStatistics statistics = new BinaryLogClientStatistics();
        statistics.onDisconnect(null);
        assertEquals(statistics.getNumberOfDisconnects(), 1L);
    }

    @Test
    public void testReset() throws Exception {
        BinaryLogClientStatistics statistics = new BinaryLogClientStatistics();
        statistics.onEventDeserializationFailure(null, null);
        statistics.onEvent(generateEvent(EventType.FORMAT_DESCRIPTION, 104, 1));
        statistics.onDisconnect(null);
        statistics.reset();
        assertNull(statistics.getLastEvent());
        assertEquals(statistics.getSecondsSinceLastEvent(), 0L);
        assertEquals(statistics.getTotalNumberOfEventsSeen(), 0L);
        assertEquals(statistics.getNumberOfSkippedEvents(), 0L);
        assertEquals(statistics.getNumberOfDisconnects(), 0L);
    }

    private Event generateEvent(EventType type, long nextPosition, long serverId) {
        EventHeaderV4 header = new EventHeaderV4();
        header.setEventType(EventType.FORMAT_DESCRIPTION);
        header.setNextPosition(104);
        header.setServerId(1);
        return new Event(header, null);
    }
}