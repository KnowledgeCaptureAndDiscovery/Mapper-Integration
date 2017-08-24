package edu.isi.wings.portal.classes.util;

import java.util.ArrayList;
import java.util.HashMap;

public class TemplateBindings {
  String templateId;
  HashMap<String, ArrayList<String>> dataBindings;
  HashMap<String, String> componentBindings;
  HashMap<String, Object> parameterBindings;
  HashMap<String, String> parameterTypes;

  public String getTemplateId() {
    return templateId;
  }

  public void setTemplateId(String templateId) {
    this.templateId = templateId;
  }

  public HashMap<String, ArrayList<String>> getDataBindings() {
    return dataBindings;
  }

  public void setDataBindings(HashMap<String, ArrayList<String>> dataBindings) {
    this.dataBindings = dataBindings;
  }

  public HashMap<String, String> getComponentBindings() {
    return componentBindings;
  }

  public void setComponentBindings(HashMap<String, String> componentBindings) {
    this.componentBindings = componentBindings;
  }

  public HashMap<String, Object> getParameterBindings() {
    return parameterBindings;
  }

  public void setParameterBindings(HashMap<String, Object> parameterBindings) {
    this.parameterBindings = parameterBindings;
  }

  public HashMap<String, String> getParameterTypes() {
    return parameterTypes;
  }

  public void setParameterTypes(HashMap<String, String> parameterTypes) {
    this.parameterTypes = parameterTypes;
  }
}
