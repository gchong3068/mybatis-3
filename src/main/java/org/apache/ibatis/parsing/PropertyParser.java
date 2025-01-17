/*
 *    Copyright 2009-2023 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.parsing;

import java.util.Properties;

/**
 * 动态属性解析器
 * @author Clinton Begin
 * @author Kazuki Shimizu
 */
public class PropertyParser {

  private static final String KEY_PREFIX = "org.apache.ibatis.parsing.PropertyParser.";
  /**
   * The special property key that indicate whether enable a default value on placeholder.
   * <p>
   * The default value is {@code false} (indicate disable a default value on placeholder) If you specify the
   * {@code true}, you can specify key and default value on placeholder (e.g. {@code ${db.username:postgres}}).
   * </p>
   *
   * @since 3.4.2
   */
  public static final String KEY_ENABLE_DEFAULT_VALUE = KEY_PREFIX + "enable-default-value";

  /**
   * The special property key that specify a separator for key and default value on placeholder.
   * <p>
   * The default separator is {@code ":"}.
   * </p>
   *
   * @since 3.4.2
   */
  public static final String KEY_DEFAULT_VALUE_SEPARATOR = KEY_PREFIX + "default-value-separator";

  private static final String ENABLE_DEFAULT_VALUE = "false";
  private static final String DEFAULT_VALUE_SEPARATOR = ":";

  private PropertyParser() {
    // Prevent Instantiation
  }

  public static String parse(String string, Properties variables) {
    //创建 VariableTokenHandler 对象
    VariableTokenHandler handler = new VariableTokenHandler(variables);
    //创建 GenericTokenParser 对象
    GenericTokenParser parser = new GenericTokenParser("${", "}", handler);
    //执行解析
    return parser.parse(string);
  }


  private static class VariableTokenHandler implements TokenHandler {
    /**
     * 变量 Properties 对象
     */
    private final Properties variables;
    /**
     * 是否开启默认值功能。默认为 {@link #ENABLE_DEFAULT_VALUE}
     */
    private final boolean enableDefaultValue;
    /**
     * 默认值的分隔符。默认为 {@link #KEY_DEFAULT_VALUE_SEPARATOR} ，即 ":" 。
     */
    private final String defaultValueSeparator;

    /* 开启默认值功能。默认为 ENABLE_DEFAULT_VALUE 配置如下
    <properties resource="org/mybatis/example/config.properties">
     <!--...-->
      <property name="org.apache.ibatis.parsing.PropertyParser.enable-default-value"value="true"/> <!--Enable this feature -->
    </properties>
    */

    private VariableTokenHandler(Properties variables) {
      this.variables = variables;
      this.enableDefaultValue = Boolean.parseBoolean(getPropertyValue(KEY_ENABLE_DEFAULT_VALUE, ENABLE_DEFAULT_VALUE));
      this.defaultValueSeparator = getPropertyValue(KEY_DEFAULT_VALUE_SEPARATOR, DEFAULT_VALUE_SEPARATOR);
    }

    /* 默认值的分隔符。默认为 KEY_DEFAULT_VALUE_SEPARATOR 即 :  配置如下(改为 ?:)
     <properties resource="org/mybatis/example/config.properties">
     <!-- ... -->
      <property name="org.apache.ibatis.parsing.PropertyParser.default-value-separator" value="?:"/> <!-- Change default value of separator -->
     </properties>
     */

    private String getPropertyValue(String key, String defaultValue) {
      return variables == null ? defaultValue : variables.getProperty(key, defaultValue);
    }



    @Override
    public String handleToken(String content) {
      if (variables != null) {
        String key = content;
        // 是否开启默认值功能
        if (enableDefaultValue) {
          final int separatorIndex = content.indexOf(defaultValueSeparator);
          String defaultValue = null;
          if (separatorIndex >= 0) {
            key = content.substring(0, separatorIndex);
            defaultValue = content.substring(separatorIndex + defaultValueSeparator.length());
          }
          if (defaultValue != null) {
            return variables.getProperty(key, defaultValue);
          }
        }
        if (variables.containsKey(key)) {
          return variables.getProperty(key);
        }
      }
      return "${" + content + "}";
    }
  }

}
