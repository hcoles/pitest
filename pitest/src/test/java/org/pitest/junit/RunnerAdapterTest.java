///*
// * Copyright 2010 Henry Coles
// * 
// * Licensed under the Apache License, Version 2.0 (the "License"); 
// * you may not use this file except in compliance with the License. 
// * You may obtain a copy of the License at 
// * 
// * http://www.apache.org/licenses/LICENSE-2.0 
// * 
// * Unless required by applicable law or agreed to in writing, 
// * software distributed under the License is distributed on an "AS IS" BASIS, 
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
// * See the License for the specific language governing permissions and limitations under the License. 
// */
//package org.pitest.junit;
//
//import static org.junit.Assert.assertEquals;
//import static org.mockito.Matchers.any;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.pitest.Description;
//import org.pitest.extension.ResultCollector;
//import org.pitest.internal.IsolationUtils;
//import org.pitest.junit.adapter.RunnerAdapter;
//
//import com.example.TheoryTest;
//
//public class RunnerAdapterTest {
//
//  @Mock
//  ResultCollector       rc;
//
//  private RunnerAdapter testee;
//
//  @Before
//  public void setup() {
//    MockitoAnnotations.initMocks(this);
//    this.testee = new RunnerAdapter(TheoryTest.class);
//  }
//
//  @Test
//  public void shouldCallsNotifyStart() {
//    this.testee.execute(IsolationUtils.getContextClassLoader(), this.rc);
//    verify(this.rc, times(3)).notifyStart(any(Description.class));
//  }
//
//  @Test
//  public void shouldCallNotifyEndOnSuccess() {
//    this.testee.execute(IsolationUtils.getContextClassLoader(), this.rc);
//    verify(this.rc, times(3)).notifyEnd(any(Description.class));
//  }
//
//  @Test
//  public void shouldSerializAndDeserializ() throws Exception {
//    final String serialized = IsolationUtils.toTransportString(this.testee);
//    final RunnerAdapter actual = (RunnerAdapter) IsolationUtils
//        .fromTransportString(serialized);
//    assertEquals(this.testee.getDescriptions().size(), actual.getDescriptions()
//        .size());
//
//  }
//
// }
