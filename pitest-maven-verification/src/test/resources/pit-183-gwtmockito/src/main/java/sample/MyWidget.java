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

import com.google.gwt.core.shared.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;

/**
 * A sample widget that would be hard to test without GwtMockito.
 */
public class MyWidget extends Composite {

  interface MyUiBinder extends UiBinder<Widget, MyWidget> {
    
  }
  private static final MyUiBinder UI_BINDER = GWT.create(MyUiBinder.class);

  public interface DataProvider {
    String getData();
  }

  @UiField HasText name;
  @UiField HasText data;

  private final DataProvider dataProvider;

  public MyWidget() {
    dataProvider = GWT.create(DataProvider.class);
    doStuffInJavaScript();
    initWidget(UI_BINDER.createAndBindUi(this));
  }

  public void setName(String firstName, String lastName) {
    name.setText(firstName + " " + lastName);
  }

  public void updateData() {
    data.setText(dataProvider.getData());
  }

  public void loadDataFromRpc() {
    MyServiceAsync service = GWT.create(MyService.class);
    service.getData(new AsyncCallback<String>() {
      @Override
      public void onSuccess(String result) {
        data.setText(result);
      }

      @Override
      public void onFailure(Throwable caught) {
        
      }
    });
  }

  native Element doStuffInJavaScript() /*-{
    $wnd.console.log("some message");
    return $doc;
  }-*/;
}
