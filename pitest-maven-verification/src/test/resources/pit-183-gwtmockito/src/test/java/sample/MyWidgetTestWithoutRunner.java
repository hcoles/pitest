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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwtmockito.GwtMock;
import com.google.gwtmockito.GwtMockito;
import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.gwtmockito.fakes.FakeProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import sample.MyWidget.DataProvider;

/**
 * Test for {@link MyWidget} that doesn't use {@link GwtMockitoTestRunner}. Most
 * things will still work, though it gets a bit more verbose. Most tests should
 * look like {@link MyWidgetTest} instead.
 */
@RunWith(JUnit4.class)
public class MyWidgetTestWithoutRunner {

  @GwtMock private DataProvider dataProvider;

  private MyWidget widget;

  @Before
  public void setUp() {
    GwtMockito.initMocks(this);
    widget = new MyWidget() {
      @Override
      protected void initWidget(Widget widget) {
        // initWidget must be disarmed when testing widget to avoid
        // UnsatisfiedLinkErrors
      }

      @Override
      Element doStuffInJavaScript() {
        // JSNI methods  must be explicitly overridden or factored out to avoid
        // UnsatisfiedLinkErrors
        return null;
      }
    };
  }

  /*
   * The remaining test cases look the same as they do in MyWidgetTest.
   */

  @Test
  public void testSetName() {
    widget.setName("John", "Smith");
    verify(widget.name).setText("John Smith");
  }

  @Test
  public void testUpdateData() {
    when(dataProvider.getData()).thenReturn("data");
    widget.updateData();
    verify(widget.data).setText("data");
  }

  @Test
  public void testSetNameWithFakes() {
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
    widget = new MyWidget() {
      @Override protected void initWidget(Widget widget) {}
      @Override Element doStuffInJavaScript() { return null; }
    };

    widget.setName("John", "Smith");
    assertEquals("John Smith", widget.name.getText());
  }
}
