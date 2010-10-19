/*
 * Copyright 2010 Henry Coles
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and limitations under the License. 
 */
package org.pitest.util;

import java.util.HashMap;
import java.util.Map;

import org.pitest.functional.SideEffect1;

import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.request.EventRequest;

public class EventQueueMonitor extends Thread {
  private final EventQueue                            eq;

  private volatile boolean                            run   = true;

  private final Map<EventRequest, SideEffect1<Event>> hooks = new HashMap<EventRequest, SideEffect1<Event>>();

  @Override
  public void run() {
    try {
      while (this.run) {

        final EventSet es = this.eq.remove();
        for (final Event each : es) {

          if (each instanceof VMDeathEvent) {
            this.run = false;
          }

          if (this.hooks.get(each.request()) != null) {
            this.hooks.get(each.request()).apply(each);
          }

        }

      }
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  public EventQueueMonitor(final EventQueue eq) {
    super();
    this.eq = eq;
    setDaemon(true);
    start();
  }

  public void addHook(final EventRequest er, final SideEffect1<Event> hook) {
    this.hooks.put(er, hook);
  }

  public void shutdown() {
    this.run = false;
  }
}
