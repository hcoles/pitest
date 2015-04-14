/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package sample;

import static com.google.gwtmockito.AsyncAnswers.returnSuccess;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwtmockito.GwtMock;
import com.google.gwtmockito.GwtMockito;
import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.gwtmockito.fakes.FakeProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import sample.MyWidget.DataProvider;

/**
 * Test for {@link MyWidget} using {@link GwtMockitoTestRunner}.
 */
@RunWith(GwtMockitoTestRunner.class)
public class MyWidgetTest {

  @GwtMock private DataProvider dataProvider;
  @GwtMock private MyServiceAsync myService;

  private MyWidget widget;

  @Before
  public void setUp() {
    widget = new MyWidget();
  }

  @Test
  public void testSetName() {
    widget.setName("John", "Smith");

    // Since name is a @UiField, it will be automatically filled with a mock
    // that we can verify here.
    verify(widget.name).setText("John Smith");
  }

  @Test
  public void testUpdateData() {
    // Since dataProvider is declared as a @GwtMock, any calls to
    // GWT.create(dataProvider.class) will return dataProvider.
    when(dataProvider.getData()).thenReturn("data");

    widget.updateData();

    verify(widget.data).setText("data");
  }

  @Test
  public void testSetNameWithFakes() {
    // If we don't want to use mocks, we can instead provide fake
    // implementations to be created. Normally you would do this in setUp.
    GwtMockito.useProviderForType(HasText.class, new FakeProvider<HasText>() {
      @Override
      public HasText getFake(Class<?> type) {
        return new HasText() {
          private String text;

          @Override
          public void setText(String text) {
            this.text = text;
          }

          @Override
          public String getText() {
            return text;
          }
        };
      }
    });
    widget = new MyWidget();

    widget.setName("John", "Smith");
    assertEquals("John Smith", widget.name.getText());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testMockRpcs() {
    doAnswer(returnSuccess("some data")).when(myService).getData(any(AsyncCallback.class));
    widget.loadDataFromRpc();
    verify(widget.data).setText("some data");
  }
}
