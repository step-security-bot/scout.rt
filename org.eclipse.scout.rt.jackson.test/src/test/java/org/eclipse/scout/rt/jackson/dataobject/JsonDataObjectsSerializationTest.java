/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.jackson.dataobject;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.eclipse.scout.rt.jackson.dataobject.fixture.TestBigIntegerDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestCollectionsDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestComplexEntityDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestComplexEntityPojo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestCoreExample1Do;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestCoreExample2Do;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestCoreExample3Do;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestDateDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestDoValuePojo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestDuplicatedAttributeDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestElectronicAddressDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestEmptyObject;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestEntityWithArrayDoValueDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestEntityWithListsDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestEntityWithNestedEntityDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestItemDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestItemPojo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestItemPojo2;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestMapDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestMixedRawBigIntegerDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestPersonDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestPhysicalAddressDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestPhysicalAddressExDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestPojoWithJacksonAnnotations;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestProjectExample1Do;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestProjectExample2Do;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestProjectExample3Do;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestRenamedAttributeDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestSetDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestStringHolder;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestStringHolderPojo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestStringPojo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestSubPojo;
import org.eclipse.scout.rt.jackson.testing.DataObjectSerializationTestHelper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.dataobject.DataObjectHelper;
import org.eclipse.scout.rt.platform.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.dataobject.DoList;
import org.eclipse.scout.rt.platform.dataobject.DoValue;
import org.eclipse.scout.rt.platform.dataobject.IValueFormatConstants;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.NumberUtility;
import org.eclipse.scout.rt.platform.util.date.DateUtility;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

/**
 * Various test cases serializing and deserializing Scout data objects from/to JSON
 */
public class JsonDataObjectsSerializationTest {

  protected static final Date DATE_TRUNCATED = DateUtility.parse("1990-10-20 00:00:00.000", IValueFormatConstants.DEFAULT_DATE_PATTERN);
  protected static final Date DATE = DateUtility.parse("2017-11-30 17:29:12.583", IValueFormatConstants.DEFAULT_DATE_PATTERN);

  protected static final UUID UUID_1 = UUID.fromString("ab8b13a4-b2a0-47a0-9d79-80039417b843");
  protected static final UUID UUID_2 = UUID.fromString("87069a20-6fc5-4b6a-9bc2-2e6cb75d7571");

  protected static final DataObjectSerializationTestHelper s_testHelper = BEANS.get(DataObjectSerializationTestHelper.class);
  protected static final DataObjectHelper s_dataObjectHelper = BEANS.get(DataObjectHelper.class);

  protected static ObjectMapper s_dataObjectMapper;
  protected static ObjectMapper s_defaultJacksonObjectMapper;

  @BeforeClass
  public static void beforeClass() {
    s_dataObjectMapper = BEANS.get(JacksonDataObjectMapper.class).getObjectMapper();

    s_defaultJacksonObjectMapper = new ObjectMapper()
        .setSerializationInclusion(Include.NON_DEFAULT)
        .setDateFormat(new SimpleDateFormat(IValueFormatConstants.DEFAULT_DATE_PATTERN))
        .enable(SerializationFeature.INDENT_OUTPUT)
        .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
        .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
  }

  // ------------------------------------ DoValue test cases ------------------------------------

  /**
   * POJO root class which contains a {@code DoValue<String>} element
   */
  @Test
  public void testSerialize_DoValuePojo() throws Exception {
    TestDoValuePojo pojo = new TestDoValuePojo();
    pojo.setStringValue(createDoValue("foo"));
    String json = s_dataObjectMapper.writeValueAsString(pojo);
    assertJsonResourceEquals("TestDoValuePojo.json", json);

    TestDoValuePojo pojoMarshalled = s_dataObjectMapper.readValue(json, TestDoValuePojo.class);
    assertEquals(pojo.getStringValue().get(), pojoMarshalled.getStringValue().get());
  }

  @Test
  public void testDeserialize_DoValuePojo() throws Exception {
    String inputJson = readResourceAsString("TestDoValuePojo.json");
    TestDoValuePojo pojo = s_dataObjectMapper.readValue(inputJson, TestDoValuePojo.class);
    assertEquals("foo", pojo.getStringValue().get());

    String json = s_dataObjectMapper.writeValueAsString(pojo);
    assertJsonResourceEquals("TestDoValuePojo.json", json);
  }

  /**
   * TestBigIntegerDo as root object (DoEntity), containing a {@code DoValue<BigInteger>} element
   */
  @Test
  public void testSerialize_TestBigIntegerDo() throws Exception {
    TestBigIntegerDo testDo = BEANS.get(TestBigIntegerDo.class);
    testDo.bigIntegerAttribute().set(new BigInteger("123456"));
    String json = s_dataObjectMapper.writeValueAsString(testDo);
    assertJsonResourceEquals("TestBigIntegerDo.json", json);

    TestBigIntegerDo pojoMarshalled = s_dataObjectMapper.readValue(json, TestBigIntegerDo.class);
    assertEquals(testDo.getBigIntegerAttribute(), pojoMarshalled.getBigIntegerAttribute());
  }

  @Test
  public void testDeserialize_TestBigIntegerDo() throws Exception {
    String inputJson = readResourceAsString("TestBigIntegerDo.json");
    TestBigIntegerDo testDo = s_dataObjectMapper.readValue(inputJson, TestBigIntegerDo.class);
    assertEquals("123456", testDo.getBigIntegerAttribute().toString());

    String json = s_dataObjectMapper.writeValueAsString(testDo);
    assertJsonResourceEquals("TestBigIntegerDo.json", json);
  }

  @Test
  public void testDeserialize_TestEntityWithNestedEntityDo() throws Exception {
    String inputJson = readResourceAsString("TestEntityWithNestedEntityDo.json");
    TestEntityWithNestedEntityDo testDo = s_dataObjectMapper.readValue(inputJson, TestEntityWithNestedEntityDo.class);
    assertEquals("123456", testDo.getBigIntegerAttribute().toString());

    String json = s_dataObjectMapper.writeValueAsString(testDo);
    assertJsonResourceEquals("TestEntityWithNestedEntityDo.json", json);
  }

  @Test
  public void testSerialize_EntityDoWithArrayDoValue() throws Exception {
    TestEntityWithArrayDoValueDo testDo = BEANS.get(TestEntityWithArrayDoValueDo.class);
    testDo.stringArrayAttribute().set(new String[]{"one", "two", "three"});
    testDo.itemDoArrayAttribute().set(new TestItemDo[]{createTestItemDo("1", "foo"), createTestItemDo("2", "bar")});
    String json = s_dataObjectMapper.writeValueAsString(testDo);
    assertJsonResourceEquals("TestEntityWithArrayDoValueDo.json", json);
  }

  @Test
  public void testDeserialize_EntityWithArrayDoValueDo() throws Exception {
    String jsonInput = readResourceAsString("TestEntityWithArrayDoValueDo.json");
    TestEntityWithArrayDoValueDo entity = s_dataObjectMapper.readValue(jsonInput, TestEntityWithArrayDoValueDo.class);

    String[] valuesWithExplicitType = entity.get("stringArrayAttribute", String[].class);
    assertArrayEquals(new String[]{"one", "two", "three"}, valuesWithExplicitType);

    String[] valuesWithInferedType = entity.get("stringArrayAttribute", String[].class);
    assertArrayEquals(new String[]{"one", "two", "three"}, valuesWithInferedType);

    TestItemDo[] itemDo = entity.get("itemDoArrayAttribute", TestItemDo[].class);
    assertEquals("1", itemDo[0].getId());
    assertEquals("foo", itemDo[0].getStringAttribute());

    assertEquals("2", entity.itemDoArrayAttribute().get()[1].getId());
    assertEquals("bar", entity.itemDoArrayAttribute().get()[1].getStringAttribute());

    String json = s_dataObjectMapper.writeValueAsString(entity);
    s_testHelper.assertJsonEquals(jsonInput, json);
  }

  @Test
  public void testSerialize_DateDo() throws Exception {
    final String dateWithTimeZoneString = "2017-11-30 17:29:12.583 +0100";
    final Date dateWithTimezone = DateUtility.parse(dateWithTimeZoneString, IValueFormatConstants.TIMESTAMP_WITH_TIMEZONE_PATTERN);
    final String dateWithTimeZoneFormattedLocal = DateUtility.format(dateWithTimezone, IValueFormatConstants.TIMESTAMP_WITH_TIMEZONE_PATTERN);

    TestDateDo testDo = BEANS.get(TestDateDo.class);
    testDo.withDateDefault(DATE)
        .withDateOnly(DATE_TRUNCATED)
        .withDateOnlyDoList(DATE_TRUNCATED)
        .withDateOnlyList(Arrays.asList(DATE_TRUNCATED))
        .withDateWithTimestamp(DATE)
        .withDateWithTimestampWithTimezone(dateWithTimezone)
        .withDateYearMonth(DATE_TRUNCATED)
        .withCustomDateFormat(DATE);

    String json = s_dataObjectMapper.writeValueAsString(testDo);
    String expectedJson = readResourceAsString("TestDateDo.json");
    // replace expected date to expect formatted date in local timezone representation
    expectedJson = expectedJson.replace(dateWithTimeZoneString, dateWithTimeZoneFormattedLocal);
    s_testHelper.assertJsonEquals(expectedJson, json);

    // deserialize and check
    TestDateDo testDoMarshalled = s_dataObjectMapper.readValue(expectedJson, TestDateDo.class);
    assertEquals(DATE, testDoMarshalled.dateDefault().get());
    assertEquals(DATE_TRUNCATED, testDoMarshalled.dateOnly().get());
    assertEquals(DATE_TRUNCATED, testDoMarshalled.dateOnlyDoList().get().get(0));
    assertEquals(DATE_TRUNCATED, testDoMarshalled.dateOnlyList().get().get(0));
    assertEquals(DATE, testDoMarshalled.dateWithTimestamp().get());
    assertEquals(dateWithTimezone, testDoMarshalled.dateWithTimestampWithTimezone().get());
    assertEquals(DateUtility.truncDateToMonth(DATE_TRUNCATED), testDoMarshalled.getDateYearMonth());
  }

  @Test(expected = JsonMappingException.class)
  public void testSerialize_InvalidDateDo() throws Exception {
    TestDateDo testDo = BEANS.get(TestDateDo.class).withInvalidDateFormat(DATE);
    s_dataObjectMapper.writeValueAsString(testDo);
  }

  /**
   * JSON file with a valid date but format pattern on TestDateDo attribute "invalidDateFormat" is invalid
   */
  @Test(expected = IllegalArgumentException.class)
  public void testDeserialize_InvalidDateDo() throws Exception {
    String expectedJson = readResourceAsString("TestInvalidDateDo.json");
    s_dataObjectMapper.readValue(expectedJson, TestDateDo.class);
  }

  /**
   * JSON file with an invalid date for TestDateDo attribute "dateDefault"
   */
  @Test(expected = InvalidFormatException.class)
  public void testDeserialize_InvalidDate2Do() throws Exception {
    String expectedJson = readResourceAsString("TestInvalidDate2Do.json");
    s_dataObjectMapper.readValue(expectedJson, TestDateDo.class);
  }

  @Test
  public void testSerializeDeserialize_RenamedAttributeDo() throws Exception {
    TestRenamedAttributeDo testDo = BEANS.get(TestRenamedAttributeDo.class)
        .withAllAttribute(new BigDecimal("42"))
        .withDateAttribute(DATE)
        .withGet("get-value")
        .withHas("has-value")
        .withPut("put-value")
        .withHashCodeAttribute(42)
        .withWaitAttribute(123)
        .withCloneAttribute(BigDecimal.ZERO, BigDecimal.ONE)
        .withFinalizeAttribute(Arrays.asList(BigInteger.ONE, BigInteger.TEN, BigInteger.ZERO));

    String json = s_dataObjectMapper.writeValueAsString(testDo);
    TestRenamedAttributeDo testDoMarshalled = s_dataObjectMapper.readValue(json, TestRenamedAttributeDo.class);
    s_testHelper.assertDoEntityEquals(testDo, testDoMarshalled);
  }

  @Test
  public void testDeserializeDuplicatedAttribute() throws Exception {
    String json = readResourceAsString("TestDuplicatedAttributeDo.json");
    TestDuplicatedAttributeDo entity = s_dataObjectMapper.readValue(json, TestDuplicatedAttributeDo.class);
    assertEquals("secondValue", entity.getStringAttribute());
    assertEquals(new BigDecimal("2.0"), entity.getBigDecimalAttribute());
    assertEquals(new BigInteger("1"), entity.getBigIntegerAttribute());
  }

  @Test
  public void testDeserializeDuplicatedAttributeRaw() throws Exception {
    String json = readResourceAsString("TestDuplicatedAttributeDoRaw.json");
    DoEntity entity = s_dataObjectMapper.readValue(json, DoEntity.class);
    assertEquals("secondValue", entity.getString("stringAttribute"));
    assertEquals(new BigDecimal("2.0"), entity.getDecimal("bigDecimalAttribute"));
    assertEquals(new BigDecimal("1"), entity.getDecimal("bigIntegerAttribute"));
  }

  // ------------------------------------ plain POJO test cases ------------------------------------

  /**
   * POJO object with two strings, using one regular setter and one with() setter method
   */
  @Test
  public void testSerializeDeserialize_TestStringPojo() throws Exception {
    TestStringPojo pojo = new TestStringPojo();
    pojo.withString("foo");
    pojo.setString2("bar");

    String json = s_dataObjectMapper.writeValueAsString(pojo);
    assertJsonResourceEquals("TestStringPojo.json", json);

    TestStringPojo testPojo = s_dataObjectMapper.readValue(json, TestStringPojo.class);
    assertEquals("foo", testPojo.getString());
    assertEquals("bar", testPojo.getString2());
  }

  @Test
  public void testSerializeDeserialize_StringHolder() throws Exception {
    TestStringHolderPojo pojo = new TestStringHolderPojo();
    pojo.setStringHolder(new TestStringHolder());
    pojo.getStringHolder().setString("foo");

    String json = s_dataObjectMapper.writeValueAsString(pojo);
    assertJsonResourceEquals("TestStringHolderPojo.json", json);

    TestStringHolderPojo testMarshalled = s_dataObjectMapper.readValue(json, TestStringHolderPojo.class);
    assertEquals("foo", testMarshalled.getStringHolder().getString());
  }

  @Test
  public void testSerializeDeserialize_PojoWithJacksonAnnotations() throws Exception {
    // custom DoObjectMapper configured like default object mapper
    final ObjectMapper customDoObjectMapper = BEANS.get(JacksonDataObjectMapper.class).createObjectMapperInstance()
        .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
        .setDateFormat(new SimpleDateFormat(IValueFormatConstants.DEFAULT_DATE_PATTERN));

    TestPojoWithJacksonAnnotations pojo = new TestPojoWithJacksonAnnotations();
    pojo.setDefaultDate(DATE);
    pojo.setFormattedDate(DATE);
    pojo.setId("object-id-1");
    pojo.setIgnoredAttribute(123);
    pojo.setRenamedAttribute("renamed-attribute-value");

    String jsonDefaultMapper = s_defaultJacksonObjectMapper.writeValueAsString(pojo);
    String jsonDoMapper = customDoObjectMapper.writeValueAsString(pojo);
    assertEquals(jsonDefaultMapper, jsonDoMapper);

    TestPojoWithJacksonAnnotations pojoMarshalledDefaultMapper = s_defaultJacksonObjectMapper.readValue(jsonDefaultMapper, TestPojoWithJacksonAnnotations.class);
    TestPojoWithJacksonAnnotations pojoMarshalledDoMapper = customDoObjectMapper.readValue(jsonDefaultMapper, TestPojoWithJacksonAnnotations.class);
    assertEquals(pojoMarshalledDefaultMapper, pojoMarshalledDoMapper);
  }

  // ------------------------------------ Raw data object test cases ------------------------------------

  @Test
  public void testSerialize_SimpleDoRaw() throws Exception {
    DoEntity testDo = new DoEntity();
    testDo.put("bigIntegerAttribute", createDoValue(new BigInteger("123456")));
    testDo.put("bigDecimalAttribute", new BigDecimal("789.0"));
    testDo.put("dateAttribute", createDoValue(DateUtility.parse("2017-09-22 14:23:12.123", IValueFormatConstants.DEFAULT_DATE_PATTERN)));

    DoEntity testDo2 = new DoEntity();
    testDo2.put("bigIntegerAttribute2", createDoValue(new BigInteger("789")));

    testDo.put("itemAttributeNode", testDo2);
    testDo.put("itemAttributeRef", testDo2);
    testDo.put("itemsAttributeList", Arrays.asList(testDo2, testDo2));
    testDo.put("attributeWithNullValue", null);
    testDo.putList("listAttributeWithNullValue", null);

    String json = s_dataObjectMapper.writeValueAsString(testDo);
    assertJsonResourceEquals("TestSimpleDoRaw.json", json);
  }

  @Test
  public void testSerialize_SimpleDoWithPojoRaw() throws Exception {
    DoEntity testDo = new DoEntity();
    testDo.put("bigIntegerAttribute", createDoValue(new BigInteger("123456")));
    testDo.put("bigDecimalAttribute", new BigDecimal("789.0"));
    testDo.put("dateAttribute", createDoValue(DateUtility.parse("2017-09-22 14:23:12.123", IValueFormatConstants.DEFAULT_DATE_PATTERN)));

    TestSubPojo sub = new TestSubPojo();
    sub.setBar("bar");
    testDo.put("sub", sub);

    DoEntity testDo2 = new DoEntity();
    testDo2.put("bigIntegerAttribute2", createDoValue(new BigInteger("789")));
    testDo.put("itemAttributeNode", testDo2);
    testDo.put("itemAttributeRef", testDo2);
    testDo.put("itemsAttributeList", Arrays.asList(testDo2, testDo2));

    String json = s_dataObjectMapper.writeValueAsString(testDo);
    assertJsonResourceEquals("TestSimpleDoWithPojoRaw.json", json);
  }

  @Test
  public void testDeserialize_SimpleDoRaw() throws Exception {
    String jsonInput = readResourceAsString("TestSimpleDoRaw.json");
    DoEntity entity = s_dataObjectMapper.readValue(jsonInput, DoEntity.class);

    // raw properties got default JSON->Java conversion types
    assertEquals(Integer.valueOf(123456), entity.get("bigIntegerAttribute"));
    assertEquals(Double.valueOf(789.0), entity.get("bigDecimalAttribute"));
    assertEquals("2017-09-22 14:23:12.123", entity.get("dateAttribute"));

    // assert "null" values raw
    assertNull(entity.get("attributeWithNullValue"));
    assertTrue(entity.getList("listAttributeWithNullValue").isEmpty());

    // assert "null" values when read as string
    assertNull(entity.getString("attributeWithNullValue"));
    assertTrue(entity.getStringList("listAttributeWithNullValue").isEmpty());

    String json = s_dataObjectMapper.writeValueAsString(entity);
    s_testHelper.assertJsonEquals(jsonInput, json);
  }

  @Test
  public void testSerialize_EmptyRawDo() throws Exception {
    DoEntity testDo = new DoEntity();
    String json = s_dataObjectMapper.writeValueAsString(testDo);
    assertJsonResourceEquals("TestEmptyDoEntity.json", json);
  }

  @Test
  public void testSerialize_EntityWithEmptyObjectDo() throws Exception {
    DoEntity testDo = new DoEntity();
    testDo.put("emptyObject", new TestEmptyObject());
    testDo.put("emptyList", Arrays.asList());
    testDo.put("emptyEntity", new DoEntity());
    String json = s_dataObjectMapper.writeValueAsString(testDo);
    assertJsonResourceEquals("TestEntityWithEmptyObjectDo.json", json);
  }

  @Test
  public void testDeserialize_EntityWithEmptyObjectDo() throws Exception {
    String input = readResourceAsString("TestEntityWithEmptyObjectDo.json");
    DoEntity entity = s_dataObjectMapper.readValue(input, DoEntity.class);
    assertTrue(entity.getList("emptyList", Object.class).isEmpty());
    DoEntity emptyObject = entity.get("emptyObject", DoEntity.class);
    assertTrue(emptyObject.all().isEmpty());
    DoEntity emptyEntity = entity.get("emptyEntity", DoEntity.class);
    assertTrue(emptyEntity.all().isEmpty());
  }

  @Test
  public void testSerialize_MixedRawDo() throws Exception {
    TestMixedRawBigIntegerDo dataObject = new TestMixedRawBigIntegerDo();
    dataObject.withBigIntegerAttribute(new BigInteger("123456"));
    dataObject.put("bigDecimalAttribute", new BigDecimal("789.0"));
    dataObject.put("dateAttribute", DateUtility.parse("2017-09-22 14:23:12.123", IValueFormatConstants.DEFAULT_DATE_PATTERN));
    dataObject.withNumberAttribute(42);

    String json = s_dataObjectMapper.writeValueAsString(dataObject);
    assertJsonResourceEquals("TestMixedRawBigIntegerDo.json", json);
  }

  @Test
  public void testDeserialize_MixedRawDo() throws Exception {
    String jsonInput = readResourceAsString("TestMixedRawBigIntegerDo.json");
    TestMixedRawBigIntegerDo mixedEntityDo = s_dataObjectMapper.readValue(jsonInput, TestMixedRawBigIntegerDo.class);

    // known properties have types according to declaration in TestMixedRawBigIntegerDo
    assertEquals(new BigInteger("123456"), mixedEntityDo.getBigIntegerAttribute());
    assertEquals(Integer.valueOf(42), mixedEntityDo.getNumberAttribute());

    // raw properties got default JSON->Java conversion types
    assertEquals(Double.valueOf(789.0), mixedEntityDo.get("bigDecimalAttribute"));
    assertEquals("2017-09-22 14:23:12.123", mixedEntityDo.get("dateAttribute"));

    String json = s_dataObjectMapper.writeValueAsString(mixedEntityDo);
    s_testHelper.assertJsonEquals(jsonInput, json);
  }

  @Test
  public void testDeserialize_MixedRawDoAsPlainRaw() throws Exception {
    String jsonInput = readResourceAsString("TestMixedRawBigIntegerDoRaw.json");
    DoEntity entity = s_dataObjectMapper.readValue(jsonInput, DoEntity.class);

    // raw properties got default JSON->Java conversion types
    assertEquals(Integer.valueOf(123456), ((DoValue<?>) entity.getNode("bigIntegerAttribute")).get());
    assertEquals(Integer.valueOf(42), ((DoValue<?>) entity.getNode("numberAttribute")).get());
    assertEquals(Double.valueOf(789.0), ((DoValue<?>) entity.getNode("bigDecimalAttribute")).get());
    assertEquals("2017-09-22 14:23:12.123", ((DoValue<?>) entity.getNode("dateAttribute")).get());

    // use accessor methods to convert value to specific type
    assertEquals(new BigInteger("123456"), s_dataObjectHelper.getBigIntegerAttribute(entity, "bigIntegerAttribute"));
    assertEquals(Integer.valueOf(42), s_dataObjectHelper.getIntegerAttribute(entity, "numberAttribute"));
    assertEquals(Double.valueOf(789.0), s_dataObjectHelper.getDoubleAttribute(entity, "bigDecimalAttribute"));
    assertEquals(new BigDecimal("789.0"), entity.getDecimal("bigDecimalAttribute"));
    assertEquals("2017-09-22 14:23:12.123", entity.getString("dateAttribute"));

    String json = s_dataObjectMapper.writeValueAsString(entity);
    s_testHelper.assertJsonEquals(jsonInput, json);
  }

  @Test
  public void testDeserialize_ComplexEntityDoRaw() throws Exception {
    String jsonInput = readResourceAsString("TestComplexEntityDoRaw.json");
    DoEntity entity = s_dataObjectMapper.readValue(jsonInput, DoEntity.class);

    TestComplexEntityDo expected = createTestDo();
    assertEquals(expected.getStringAttribute(), entity.getString("stringAttribute"));
    assertEquals(expected.getIntegerAttribute(), s_dataObjectHelper.getIntegerAttribute(entity, "integerAttribute"));
    assertEquals(expected.getDoubleAttribute(), s_dataObjectHelper.getDoubleAttribute(entity, "doubleAttribute"));
    assertEquals(expected.getStringListAttribute(), entity.getList("stringListAttribute", String.class));

    // floating point values are converted to Double
    assertEquals(expected.getFloatAttribute().floatValue(), entity.get("floatAttribute", Double.class).floatValue(), 0);
    assertEquals(expected.getBigDecimalAttribute(), NumberUtility.toBigDecimal(entity.get("bigDecimalAttribute", Double.class)));
    assertEquals(expected.getBigDecimalAttribute(), entity.getDecimal("bigDecimalAttribute"));
    // short integer/long values are converted to Integer
    assertEquals(expected.getBigIntegerAttribute(), NumberUtility.toBigInteger(entity.get("bigIntegerAttribute", Integer.class).longValue()));
    assertEquals(expected.getLongAttribute().longValue(), entity.get("longAttribute", Integer.class).longValue());
    // date value is converted to String
    assertEquals(expected.getDateAttribute(), DateUtility.parse(entity.get("dateAttribute", String.class), IValueFormatConstants.TIMESTAMP_PATTERN));
    assertEquals(expected.getDateAttribute(), s_dataObjectHelper.getDateAttribute(entity, "dateAttribute"));

    // UUID value is converted to String
    assertEquals(expected.getUuidAttribute(), s_dataObjectHelper.getUuidAttribute(entity, "uuidAttribute"));

    // Locale value is converted to String
    assertEquals(expected.getLocaleAttribute(), s_dataObjectHelper.getLocaleAttribute(entity, "localeAttribute"));

    // check nested DoEntity
    DoEntity itemAttribute = s_dataObjectHelper.getEntityAttribute(entity, "itemAttribute");
    assertEquals(expected.getItemAttribute().getId(), itemAttribute.getString("id"));
    assertEquals(expected.getItemAttribute().getStringAttribute(), itemAttribute.getString("stringAttribute"));

    // nested List<DoEntity>
    List<DoEntity> itemsAttribute = entity.getList("itemsAttribute", DoEntity.class);
    assertEquals(expected.getItemsAttribute().get(0).getId(), itemsAttribute.get(0).get("id", String.class));
    assertEquals(expected.getItemsAttribute().get(1).getId(), itemsAttribute.get(1).get("id", String.class));

    // check roundtrip back to JSON
    String json = s_dataObjectMapper.writeValueAsString(entity);
    s_testHelper.assertJsonEquals(jsonInput, json);
  }

  @Test
  public void testDeserialze_EntityWithNestedDoNodeRaw() throws Exception {
    String jsonInput = readResourceAsString("TestEntityWithNestedDoNodeRaw.json");
    DoEntity entity = s_dataObjectMapper.readValue(jsonInput, DoEntity.class);

    TestComplexEntityDo testDo = BEANS.get(TestComplexEntityDo.class);
    testDo.itemAttribute().set(BEANS.get(TestItemDo.class).withId("1234-3").withStringAttribute("bar"));

    s_testHelper.assertDoEntityEquals(testDo, entity, false);
  }

  /**
   * {@code DoValue<String[]>} read as raw JSON is converted to {@code List<String>}
   */
  @Test
  public void testDeserialize_EntityWithArrayDoValueDoRaw() throws Exception {
    String jsonInput = readResourceAsString("TestEntityWithArrayDoValueDoRaw.json");
    DoEntity entity = s_dataObjectMapper.readValue(jsonInput, DoEntity.class);

    final List<String> expected = Arrays.asList("one", "two", "three");

    List<String> valuesWithExplicitType = entity.getList("stringArrayAttribute", String.class);
    assertEquals(expected, valuesWithExplicitType);

    List<String> valuesWithInferedType = entity.getList("stringArrayAttribute", String.class);
    assertEquals(expected, valuesWithInferedType);

    List<DoEntity> itemDoArray = entity.getList("itemDoArrayAttribute", DoEntity.class);
    assertEquals("1", itemDoArray.get(0).get("id"));
    assertEquals("foo", itemDoArray.get(0).get("stringAttribute"));

    String json = s_dataObjectMapper.writeValueAsString(entity);
    s_testHelper.assertJsonEquals(jsonInput, json);
  }

  @Test
  public void testDeserialize_EntityWithoutTypeRaw() throws Exception {
    String inputJson = readResourceAsString("TestBigIntegerDoWithoutType.json");
    DoEntity testDo = s_dataObjectMapper.readValue(inputJson, DoEntity.class);

    // BigInteger is converted to integer when read as raw value
    assertEquals(Integer.valueOf(123456), testDo.get("bigIntegerAttribute"));

    String json = s_dataObjectMapper.writeValueAsString(testDo);
    assertJsonResourceEquals("TestBigIntegerDoRaw.json", json); // is written without type information
  }

  // ------------------------------------ Raw data object test cases with type name ------------------------

  @Test
  public void testSerialize_TypedEntity() throws Exception {
    DoTypedEntity typedEntity = BEANS.get(DoTypedEntity.class);
    typedEntity.withType("TestMyCustomType");
    typedEntity.put("date", DATE);
    typedEntity.put("string", "foo");
    typedEntity.put("integer", 42);
    String json = s_dataObjectMapper.writeValueAsString(typedEntity);
    assertJsonResourceEquals("TestMyCustomTypeDo.json", json); // is written with type information
  }

  @Test
  public void testSerialize_EmptyTypedEntity() throws Exception {
    DoTypedEntity typedEntity = BEANS.get(DoTypedEntity.class);
    typedEntity.withType("TestMyCustomTypeEmpty");
    String json = s_dataObjectMapper.writeValueAsString(typedEntity);
    assertJsonResourceEquals("TestMyCustomTypeEmptyDo.json", json); // is written with type information
  }

  @Test
  public void testDeserialize_TypedEntity() throws Exception {
    String inputJson = readResourceAsString("TestMyCustomTypeDo.json");
    DoEntity typedEntity = s_dataObjectMapper.readValue(inputJson, DoEntity.class);
    assertTrue(typedEntity instanceof DoTypedEntity);

    assertEquals("TestMyCustomType", ((DoTypedEntity) typedEntity).getType());
    assertEquals("TestMyCustomType", typedEntity.get(DataObjectTypeResolverBuilder.JSON_TYPE_PROPERTY));
    assertEquals("TestMyCustomType", typedEntity.get(((DoTypedEntity) typedEntity).type().getAttributeName()));

    assertEquals(DATE, s_dataObjectHelper.getDateAttribute(typedEntity, "date"));
    assertEquals("foo", typedEntity.get("string"));
    assertEquals(new BigDecimal("42"), typedEntity.getDecimal("integer"));

    String json = s_dataObjectMapper.writeValueAsString(typedEntity);
    assertJsonResourceEquals("TestMyCustomTypeDo.json", json); // is written with type information
  }

  @Test
  public void testDeserialize_EmptyTypedEntity() throws Exception {
    String inputJson = readResourceAsString("TestMyCustomTypeEmptyDo.json");
    DoEntity typedEntity = s_dataObjectMapper.readValue(inputJson, DoEntity.class);
    assertTrue(typedEntity instanceof DoTypedEntity);

    assertEquals("TestMyCustomTypeEmpty", ((DoTypedEntity) typedEntity).getType());
    String json = s_dataObjectMapper.writeValueAsString(typedEntity);
    assertJsonResourceEquals("TestMyCustomTypeEmptyDo.json", json); // is written with type information
  }

  // ------------------------------------ DoEntity with list test cases ------------------------------------

  @Test
  public void testSerialize_EntityWithLists() throws Exception {
    TestEntityWithListsDo testDo = new TestEntityWithListsDo();

    List<TestItemDo> list = new ArrayList<>();
    list.add(createTestItemDo("foo-ID-1", "bar-string-attribute-1"));
    list.add(BEANS.get(TestItemDo.class).withId("foo-ID-2").withStringAttribute("bar-string-attribute-2"));
    testDo.withItemsListAttribute(list);

    testDo.withItemsDoListAttribute(
        createTestItemDo("foo-ID-3", "bar-string-attribute-3"),
        createTestItemDo("foo-ID-4", "bar-string-attribute-4"));

    testDo.withStringListAttribute(Arrays.asList("stringA", "stringB"));
    testDo.withStringDoListAttribute("stringC", "stringD");

    String json = s_dataObjectMapper.writeValueAsString(testDo);
    assertJsonResourceEquals("TestEntityWithListsDo.json", json);
  }

  @Test
  public void testDeserialize_EntityWithLists() throws Exception {
    String json = readResourceAsString("TestEntityWithListsDo.json");
    TestEntityWithListsDo testDo = s_dataObjectMapper.readValue(json, TestEntityWithListsDo.class);
    assertEquals("foo-ID-1", testDo.getItemsListAttribute().get(0).getId());
    assertEquals("foo-ID-2", testDo.getItemsListAttribute().get(1).getId());
    assertEquals("foo-ID-3", testDo.getItemsDoListAttribute().get(0).getId());
    assertEquals("foo-ID-4", testDo.getItemsDoListAttribute().get(1).getId());
    assertEquals("stringA", testDo.getStringListAttribute().get(0));
    assertEquals("stringB", testDo.getStringListAttribute().get(1));
    assertEquals("stringC", testDo.getStringDoListAttribute().get(0));
    assertEquals("stringD", testDo.getStringDoListAttribute().get(1));
  }

  @Test
  public void testSerialize_EntityWithEmptyLists() throws Exception {
    TestEntityWithListsDo testDo = new TestEntityWithListsDo();
    List<TestItemDo> list = new ArrayList<>();
    testDo.withItemsListAttribute(list);
    testDo.withItemsDoListAttribute(list);
    testDo.stringListAttribute().create();
    testDo.stringDoListAttribute().create();
    String json = s_dataObjectMapper.writeValueAsString(testDo);
    assertJsonResourceEquals("TestEntityWithEmptyListsDo.json", json);
  }

  @Test
  public void testSerialize_EntityWithOneEmptyList() throws Exception {
    TestEntityWithListsDo testDo = new TestEntityWithListsDo();
    List<TestItemDo> list = new ArrayList<>();
    testDo.withItemsListAttribute(list);
    String json = s_dataObjectMapper.writeValueAsString(testDo);
    assertJsonResourceEquals("TestEntityWithOneEmptyListDo.json", json);
  }

  @Test
  public void testDeserialize_EntityWithEmptyLists() throws Exception {
    String json = readResourceAsString("TestEntityWithEmptyListsDo.json");
    TestEntityWithListsDo testDo = s_dataObjectMapper.readValue(json, TestEntityWithListsDo.class);
    assertTrue(testDo.getItemsListAttribute().isEmpty());
    assertTrue(testDo.getItemsDoListAttribute().isEmpty());
    assertTrue(testDo.stringListAttribute().exists());
    assertNull(testDo.stringListAttribute().get());
    assertTrue(testDo.getStringDoListAttribute().isEmpty());
  }

  @Test
  public void testSerialize_EmptyDoList() throws Exception {
    DoList<String> list = new DoList<>();
    String json = s_dataObjectMapper.writeValueAsString(list);
    assertJsonResourceEquals("TestEmptyDoList.json", json);
  }

  @Test
  public void testDeserialize_EmptyDoList() throws Exception {
    String json = readResourceAsString("TestEmptyDoList.json");
    DoList<?> testDo = s_dataObjectMapper.readValue(json, DoList.class);
    assertTrue(testDo.isEmpty());
  }

  @Test
  public void testSerialize_StringDoList() throws Exception {
    DoList<String> list = new DoList<>();
    list.add("foo");
    String json = s_dataObjectMapper.writeValueAsString(list);
    assertJsonResourceEquals("TestStringDoList.json", json);
  }

  @Test
  public void testDeserialize_StringDoList() throws Exception {
    String json = readResourceAsString("TestStringDoList.json");
    @SuppressWarnings("unchecked")
    DoList<String> testDo = s_dataObjectMapper.readValue(json, DoList.class);
    assertEquals("foo", testDo.get(0));
  }

  @Test
  public void testSerialize_TestItemDoList() throws Exception {
    DoList<TestItemDo> list = new DoList<>();
    list.add(createTestItemDo("foo", "bar"));
    String json = s_dataObjectMapper.writeValueAsString(list);
    assertJsonResourceEquals("TestItemDoList.json", json);
  }

  @Test
  public void testDeserialize_TestItemDoList() throws Exception {
    String json = readResourceAsString("TestItemDoList.json");
    @SuppressWarnings("unchecked")
    DoList<TestItemDo> testDo = s_dataObjectMapper.readValue(json, DoList.class);
    assertEquals("foo", testDo.get(0).getId());
    assertEquals("bar", testDo.get(0).getStringAttribute());
  }

  // ------------------------------------ Complex DoEntity test cases ------------------------------------

  @Test
  public void testSerialize_ComplexDoEntity() throws Exception {
    TestComplexEntityDo testDo = createTestDo();
    String doJson = s_dataObjectMapper.writeValueAsString(testDo);
    assertJsonResourceEquals("TestComplexEntityDo.json", doJson);

    // comparison with plain jackson object mapper and POJO object -> must result in same JSON
    ObjectMapper defaultOm = s_defaultJacksonObjectMapper;
    TestComplexEntityPojo testPoJo = createTestPoJo();
    String pojoJson = defaultOm.writeValueAsString(testPoJo);
    assertJsonResourceEquals("TestComplexEntityDo.json", doJson);
    assertEquals(doJson, pojoJson);
  }

  @Test
  public void testDeserialize_ComplexEntityDo() throws Exception {
    String jsonInput = readResourceAsString("TestComplexEntityDo.json");
    TestComplexEntityDo testDo = s_dataObjectMapper.readValue(jsonInput, TestComplexEntityDo.class);
    TestComplexEntityDo testDoExpected = createTestDo();
    s_testHelper.assertDoEntityEquals(testDoExpected, testDo);
  }

  @Test
  public void testDeserialize_EntityWithoutType() throws Exception {
    String inputJson = readResourceAsString("TestBigIntegerDoWithoutType.json");
    TestBigIntegerDo testDo = s_dataObjectMapper.readValue(inputJson, TestBigIntegerDo.class);
    assertEquals(new BigInteger("123456"), testDo.getBigIntegerAttribute());

    String json = s_dataObjectMapper.writeValueAsString(testDo);
    assertJsonResourceEquals("TestBigIntegerDo.json", json);
  }

  // ------------------------------------ DoEntity with collections test cases ------------------------------------

  @Test
  public void testSerialize_TestCollectionsDo() throws Exception {
    TestCollectionsDo testDo = createTestCollectionsDo();
    String json = s_dataObjectMapper.writeValueAsString(testDo);
    assertJsonResourceEquals("TestCollectionsDo.json", json);
  }

  @Test
  public void testDeserialize_TestCollectionsDo() throws Exception {
    String json = readResourceAsString("TestCollectionsDo.json");
    TestCollectionsDo doMarhalled = s_dataObjectMapper.readValue(json, TestCollectionsDo.class);
    TestCollectionsDo expectedDo = createTestCollectionsDo();
    s_testHelper.assertDoEntityEquals(expectedDo, doMarhalled);
  }

  @Test
  public void testSerializeDeserialize_TestCollectionsDoRaw() throws Exception {
    String json = readResourceAsString("TestCollectionsDoRaw.json");
    DoEntity doMarhalled = s_dataObjectMapper.readValue(json, DoEntity.class);

    TestCollectionsDo expectedDo = createTestCollectionsDo();
    assertEquals(expectedDo.getItemDoAttribute().getId(), doMarhalled.get("itemDoAttribute", DoEntity.class).get("id"));

    List<DoEntity> list = doMarhalled.getList("itemDoListAttribute", DoEntity.class);
    assertEquals(expectedDo.getItemListAttribute().get(0).getId(), list.get(0).get("id"));

    String serialized = s_dataObjectMapper.writeValueAsString(doMarhalled);
    s_testHelper.assertJsonEquals(json, serialized);
  }

  protected TestCollectionsDo createTestCollectionsDo() {
    TestCollectionsDo testDo = BEANS.get(TestCollectionsDo.class);

    // setup TestItemDo attributes
    testDo.withItemDoAttribute(createTestItemDo("d1", "itemDo-as-attribute"));
    testDo.withItemDoCollectionAttribute(Arrays.asList(createTestItemDo("d2", "itemDo-as-collection-item-1"), createTestItemDo("d3", "itemDo-as-collection-item-2")));
    testDo.withItemListAttribute(Arrays.asList(createTestItemDo("d4", "itemDo-as-list-item-1"), createTestItemDo("d5", "itemDo-as-list-item-2")));
    testDo.withItemDoListAttribute(createTestItemDo("d8", "itemDo-as-DoList-item-1"), createTestItemDo("d9", "itemDo-as-DoList-item-2"));

    // setup TestItemPojo attributes
    testDo.withItemPojoAttribute(createTestItemPojo("p1", "itemPojo-as-attribute"));
    testDo.withItemPojoCollectionAttribute(Arrays.asList(createTestItemPojo("p2", "itemPojo-as-collection-item-1"), createTestItemPojo("p3", "itemPojo-as-collection-item-2")));
    testDo.withItemPojoListAttribute(Arrays.asList(createTestItemPojo("p4", "itemPojo-as-list-item-1"), createTestItemPojo("p5", "itemPojo-as-list-item-2")));
    testDo.withItemPojoDoListAttribute(createTestItemPojo("p8", "itemPojo-as-DoList-item-1"), createTestItemPojo("p9", "itemPojo-as-DoList-item-2"));
    testDo.withItemPojo2DoListAttribute(createTestItemPojo2("p10", "itemPojo2-as-DoList-item-1"), createTestItemPojo2("p11", "itemPojo2-as-DoList-item-2"));

    return testDo;
  }

  @Test
  public void testSerializeDeserialize_EntityWithCollectionRaw() throws Exception {
    DoEntity entity = new DoEntity();
    entity.put("attribute1", Arrays.asList("list-item-1", "list-item-2"));
    entity.put("attribute2", Arrays.asList(123, 45.69));
    entity.put("attribute3", Arrays.asList(UUID_1, UUID_2));
    entity.put("attribute4", Arrays.asList(DATE, DATE_TRUNCATED));
    entity.put("attribute5", Arrays.asList(createTestItemDo("item-do-key-1", "item-do-value-1"), createTestItemDo("item-do-key-2", "item-do-value-2")));
    entity.put("attribute6", Arrays.asList(createTestItemDo("item-do-key-3", "item-do-value-3"), "bar"));
    entity.put("attribute7", Arrays.asList(createTestItemPojo2("item-pojo-key-1", "item-pojo-value-1"), createTestItemPojo2("item-pojo-key-2", "item-pojo-value-2")));

    String json = s_dataObjectMapper.writeValueAsString(entity);
    assertJsonResourceEquals("TestEntityWithCollectionRaw.json", json);

    DoEntity doMarshalled = s_dataObjectMapper.readValue(json, DoEntity.class);
    assertEquals("list-item-1", doMarshalled.get("attribute1", List.class).get(0));
    assertEquals("list-item-2", doMarshalled.get("attribute1", List.class).get(1));

    assertEquals(123, doMarshalled.get("attribute2", List.class).get(0));
    assertEquals(45.69, doMarshalled.get("attribute2", List.class).get(1));

    List<UUID> attribute3 = doMarshalled.getList("attribute3", item -> UUID.fromString((String) item));
    assertEquals(UUID_1, attribute3.get(0));
    assertEquals(UUID_2, attribute3.get(1));

    List<Date> attribute4 = doMarshalled.getList("attribute4", IValueFormatConstants.parseDefaultDate);
    assertEquals(DATE, attribute4.get(0));
    assertEquals(DATE_TRUNCATED, attribute4.get(1));

    List<TestItemDo> attribute5 = doMarshalled.getList("attribute5", TestItemDo.class);
    assertEquals("item-do-key-1", attribute5.get(0).getId());
    assertEquals("item-do-key-2", attribute5.get(1).getId());

    List<Object> attribute6 = doMarshalled.getList("attribute6");
    assertEquals("item-do-key-3", ((TestItemDo) attribute6.get(0)).getId());
    assertEquals("bar", attribute6.get(1));

    // TestItemPojo2 is deserialized as DoTypedEntity with type 'TestItem2'
    List<DoTypedEntity> attribute7 = doMarshalled.getList("attribute7", DoTypedEntity.class);
    assertEquals("item-pojo-key-1", attribute7.get(0).get("id"));
    assertEquals("TestItem2", attribute7.get(0).getType());
    assertEquals("item-pojo-key-2", attribute7.get(1).get("id"));
    assertEquals("TestItem2", attribute7.get(1).getType());
  }

  @Test
  public void testSerializeDeserialize_TestMapDo() throws Exception {
    TestMapDo mapDo = new TestMapDo();
    Map<String, String> stringStringMap = new HashMap<>();
    stringStringMap.put("foo1", "bar");
    stringStringMap.put("foo2", "baz");
    mapDo.withStringStringMapAttribute(stringStringMap);

    Map<Integer, Integer> integerIntegerMap = new HashMap<>();
    integerIntegerMap.put(1, 42);
    integerIntegerMap.put(2, 21);
    mapDo.withIntegerIntegerMapAttribute(integerIntegerMap);

    Map<String, TestItemPojo> stringPojoMap = new HashMap<>();
    stringPojoMap.put("pojoKey1", createTestItemPojo("item-key1", "value1"));
    stringPojoMap.put("pojoKey2", createTestItemPojo("item-key2", "value2"));
    mapDo.withStringTestItemPojoMapAttribute(stringPojoMap);

    Map<String, TestItemDo> stringDoMap = new HashMap<>();
    stringDoMap.put("doKey1", createTestItemDo("item-key3", "value3"));
    stringDoMap.put("doKey2", createTestItemDo("item-key4", "value4"));
    mapDo.withStringDoTestItemMapAttribute(stringDoMap);

    Map<Double, TestItemDo> doubleDoMap = new HashMap<>();
    doubleDoMap.put(1.11, createTestItemDo("item-key5", "value5"));
    doubleDoMap.put(2.22, createTestItemDo("item-key6", "value6"));
    mapDo.withDoubleTestItemDoMapAttribute(doubleDoMap);

    Map<Date, UUID> dateUUIDMap = new LinkedHashMap<>();
    dateUUIDMap.put(DATE, UUID_1);
    dateUUIDMap.put(DATE_TRUNCATED, UUID_2);
    mapDo.withDateUUIDMapAttribute(dateUUIDMap);

    String json = s_dataObjectMapper.writeValueAsString(mapDo);
    assertJsonResourceEquals("TestMapDo.json", json);

    TestMapDo marshalled = s_dataObjectMapper.readValue(json, TestMapDo.class);
    s_testHelper.assertDoEntityEquals(mapDo, marshalled);
  }

  @Test
  public void testSerialize_illegalKeyTypeMap() throws Exception {
    DoEntity entity = new DoEntity();
    Map<TestItemDo, String> illegalKeyTypeMap = new HashMap<>();
    illegalKeyTypeMap.put(createTestItemDo("key", "value"), "foo");
    entity.put("mapAttribute", illegalKeyTypeMap);
    String json = s_dataObjectMapper.writeValueAsString(entity);
    DoEntity marshalled = s_dataObjectMapper.readValue(json, DoEntity.class);

    DoEntity marshalledMapAttribute = marshalled.get("mapAttribute", DoEntity.class);
    assertEquals(illegalKeyTypeMap.values().iterator().next(), marshalledMapAttribute.all().values().iterator().next().get());
    assertNotEquals(illegalKeyTypeMap.keySet().iterator().next(), marshalledMapAttribute.all().keySet().iterator().next()); // TestItemDo cannot be used as key, is serialized using toString() default serializer
  }

  @Test(expected = JsonMappingException.class)
  public void testSerialize_nullKeyMap() throws Exception {
    DoEntity entity = new DoEntity();
    entity.put("mapAttribute", Collections.singletonMap(null, "foo"));
    s_dataObjectMapper.writeValueAsString(entity);
  }

  @Test
  public void testSerializeDeserialize_EntityWithMapRaw() throws Exception {
    DoEntity entity = new DoEntity();
    entity.put("mapAttribute1", Collections.singletonMap("key", "value"));
    entity.put("mapAttribute2", Collections.singletonMap(123, 45.69));
    entity.put("mapAttribute3", Collections.singletonMap(UUID_1, DATE));
    entity.put("mapAttribute4", Collections.singletonMap("foo", createTestItemDo("key", "value")));

    String json = s_dataObjectMapper.writeValueAsString(entity);
    assertJsonResourceEquals("TestEntityWithMapRaw.json", json);

    DoEntity marshalled = s_dataObjectMapper.readValue(json, DoEntity.class);

    // raw Map attributes are deserialized as DoEntity, since no concrete type information about correct Map type is available
    DoEntity attribute1 = marshalled.get("mapAttribute1", DoEntity.class);
    assertEquals("value", attribute1.get("key"));
    DoEntity attribute2 = marshalled.get("mapAttribute2", DoEntity.class);
    assertEquals(45.69, attribute2.get("123"));
    DoEntity attribute3 = marshalled.get("mapAttribute3", DoEntity.class);
    assertEquals(DATE, IValueFormatConstants.parseDefaultDate.apply(attribute3.get(UUID_1.toString())));
    DoEntity attribute4 = marshalled.get("mapAttribute4", DoEntity.class);
    assertEquals("key", attribute4.get("foo", TestItemDo.class).getId());
    assertEquals("value", attribute4.get("foo", TestItemDo.class).getStringAttribute());
  }

  @Test
  public void testSerializeDeserialize_TestSetDo() throws Exception {
    TestSetDo setDo = new TestSetDo();
    Set<String> stringSet = new LinkedHashSet<>();
    stringSet.add("foo");
    stringSet.add("bar");
    setDo.withStringSetAttribute(stringSet);

    Set<Integer> integerSet = new LinkedHashSet<>();
    integerSet.add(21);
    integerSet.add(42);
    setDo.withIntegerSetAttribute(integerSet);

    Set<TestItemPojo> pojoSet = new LinkedHashSet<>();
    pojoSet.add(createTestItemPojo("item-key1", "value1"));
    pojoSet.add(createTestItemPojo("item-key2", "value2"));
    setDo.withItemPojoSetAttribute(pojoSet);

    Set<TestItemDo> doSet = new LinkedHashSet<>();
    doSet.add(createTestItemDo("item-key3", "value3"));
    doSet.add(createTestItemDo("item-key4", "value4"));
    setDo.withItemDoSetAttribute(doSet);

    Set<Date> dateSet = new LinkedHashSet<>();
    dateSet.add(DATE);
    dateSet.add(DATE_TRUNCATED);
    setDo.withDateSetAttribute(dateSet);

    String json = s_dataObjectMapper.writeValueAsString(setDo);
    assertJsonResourceEquals("TestSetDo.json", json);

    TestSetDo marshalled = s_dataObjectMapper.readValue(json, TestSetDo.class);

    // set with primitive type must be unordered equals
    assertTrue(CollectionUtility.equalsCollection(setDo.getDateSetAttribute(), marshalled.getDateSetAttribute(), false));
    assertTrue(CollectionUtility.equalsCollection(setDo.getIntegerSetAttribute(), marshalled.getIntegerSetAttribute(), false));
    assertTrue(CollectionUtility.equalsCollection(setDo.getStringSetAttribute(), marshalled.getStringSetAttribute(), false));

    // set of TestItemDo must be unordered equals
    List<TestItemDo> expected = new ArrayList<>(setDo.getItemDoSetAttribute());
    Collections.sort(expected, Comparator.comparing(TestItemDo::getId));
    List<TestItemDo> actual = new ArrayList<>(marshalled.getItemDoSetAttribute());
    Collections.sort(actual, Comparator.comparing(TestItemDo::getId));
    for (int i = 0; i < expected.size(); i++) {
      s_testHelper.assertDoEntityEquals(expected.get(i), actual.get(i));
    }

    // set of TestItemPojo must be unordered equals
    List<TestItemPojo> expectedPojo = new ArrayList<>(setDo.getItemPojoSetAttribute());
    Collections.sort(expectedPojo, Comparator.comparing(TestItemPojo::getId));
    List<TestItemPojo> actualPojo = new ArrayList<>(marshalled.getItemPojoSetAttribute());
    Collections.sort(actualPojo, Comparator.comparing(TestItemPojo::getId));
    for (int i = 0; i < expectedPojo.size(); i++) {
      assertEquals(expectedPojo.get(i).getId(), actualPojo.get(i).getId());
      assertEquals(expectedPojo.get(i).getStringAttribute(), actualPojo.get(i).getStringAttribute());
    }
  }

  @Test
  public void testSerializeDeserialize_EntityWithSetRaw() throws Exception {
    DoEntity entity = new DoEntity();
    Set<String> stringSet = new LinkedHashSet<>();
    stringSet.add("foo");
    stringSet.add("bar");
    entity.put("setAttribute", stringSet);
    String json = s_dataObjectMapper.writeValueAsString(entity);
    DoEntity marshalled = s_dataObjectMapper.readValue(json, DoEntity.class);

    // Set is deserialized as list if no type information is available
    List<String> setAttribute = marshalled.getStringList("setAttribute");
    assertTrue(CollectionUtility.equalsCollection(stringSet, setAttribute, false));
  }

  @Test
  public void testSerializeDeserialize_TestSetDoWithDuplicateValues() throws Exception {
    // simulate JSON for TestSetDo which contains duplicated values for stringSetAttribute
    DoTypedEntity setDo = new DoTypedEntity();
    setDo.withType("TestSet");
    List<String> stringList = new ArrayList<>();
    stringList.add("foo");
    stringList.add("bar");
    stringList.add("foo");
    setDo.put("stringSetAttribute", stringList);

    String json = s_dataObjectMapper.writeValueAsString(setDo);
    TestSetDo marshalled = s_dataObjectMapper.readValue(json, TestSetDo.class);

    // duplicated values are eliminated when deserializing JSON to TestSetDo since stringSetAttribute has Set as java type
    assertEquals(2, marshalled.getStringSetAttribute().size());
    assertTrue(marshalled.getStringSetAttribute().contains("foo"));
    assertTrue(marshalled.getStringSetAttribute().contains("bar"));
  }

  // ------------------------------------ polymorphic test cases ------------------------------------

  /**
   * <pre>
                +--------------------+
                | AbstractAddressDo  |
                +---------^----------+
                          |
              +-----------+--------------+
              |                          |
     +--------+-----------+   +----------+---------+
     |ElectronicAddressDo |   | PhysicalAddressDo  |
     +--------------------+   +--------------------+
   * </pre>
   */
  @Test
  public void testSerialize_TestPersonDo() throws Exception {
    TestPersonDo personDo = createTestPersonDo();
    String json = s_dataObjectMapper.writeValueAsString(personDo);
    assertJsonResourceEquals("TestPersonDo.json", json);
  }

  @Test
  public void testDeserialize_TestPersonDo() throws Exception {
    String input = readResourceAsString("TestPersonDo.json");
    TestPersonDo personDo = s_dataObjectMapper.readValue(input, TestPersonDo.class);
    assertTestPersonDo(personDo);
  }

  protected TestPersonDo createTestPersonDo() {
    TestPersonDo personDo = BEANS.get(TestPersonDo.class);
    personDo.withBirthday(DATE_TRUNCATED);
    TestElectronicAddressDo electronicAddress = BEANS.get(TestElectronicAddressDo.class).withId("elecAddress");
    electronicAddress.email().set("foo@bar.de");
    TestPhysicalAddressDo physicalAddress = BEANS.get(TestPhysicalAddressDo.class).withId("physicAddress");
    physicalAddress.city().set("Example");
    TestPhysicalAddressExDo physicalAddressEx = BEANS.get(TestPhysicalAddressExDo.class).withId("physicAddressEx");
    physicalAddressEx.poBox().set("1234");
    personDo.withDefaultAddress(electronicAddress);
    personDo.withAddresses(Arrays.asList(electronicAddress, physicalAddress, physicalAddressEx));
    return personDo;
  }

  protected void assertTestPersonDo(TestPersonDo actual) {
    TestPersonDo expected = createTestPersonDo();
    s_testHelper.assertDoEntityEquals(expected, actual);

    assertEquals(expected.birthday().get(), actual.getBirthday());
    assertEquals(expected.birthday().get().getTime(), actual.getBirthday().getTime());

    assertTrue(actual.getDefaultAddress() instanceof TestElectronicAddressDo);
    assertEquals(expected.getDefaultAddress().getId(), ((TestElectronicAddressDo) actual.getDefaultAddress()).getId());
    assertEquals(expected.getDefaultAddress().get("email"), ((TestElectronicAddressDo) actual.getDefaultAddress()).email().get());

    assertTrue(actual.getAddresses().get(0) instanceof TestElectronicAddressDo);
    assertEquals(expected.getAddresses().get(0).getId(), ((TestElectronicAddressDo) actual.getAddresses().get(0)).getId());
    assertEquals(expected.getAddresses().get(0).get("email"), ((TestElectronicAddressDo) actual.getAddresses().get(0)).email().get());

    assertTrue(actual.getAddresses().get(1) instanceof TestPhysicalAddressDo);
    assertEquals(expected.getAddresses().get(1).getId(), ((TestPhysicalAddressDo) actual.getAddresses().get(1)).getId());
    assertEquals(expected.getAddresses().get(1).get("city"), ((TestPhysicalAddressDo) actual.getAddresses().get(1)).city().get());

    assertTrue(actual.getAddresses().get(2) instanceof TestPhysicalAddressExDo);
    assertEquals(expected.getAddresses().get(2).getId(), ((TestPhysicalAddressDo) actual.getAddresses().get(2)).getId());
    assertEquals(expected.getAddresses().get(2).get("poBox"), ((TestPhysicalAddressExDo) actual.getAddresses().get(2)).poBox().get());
  }

  @Test
  public void testSerializeDeserialize_DoEntityRawWithNestedPersons() throws Exception {
    DoEntity entity = BEANS.get(DoEntity.class);
    TestPersonDo person = createTestPersonDo();
    entity.put("person", person);
    entity.putList("persons", Arrays.asList(person, person));
    String json = s_dataObjectMapper.writeValueAsString(entity);

    DoEntity entityMarshalled = s_dataObjectMapper.readValue(json, DoEntity.class);
    assertEquals(person.birthday().get(), entityMarshalled.get("person", TestPersonDo.class).getBirthday());
    assertTestPersonDo(entityMarshalled.get("person", TestPersonDo.class));
    assertTestPersonDo(entityMarshalled.getList("persons", TestPersonDo.class).get(0));
    assertTestPersonDo(entityMarshalled.getList("persons", TestPersonDo.class).get(1));
  }

  // ------------------------------------ replacement / extensibility test cases ------------------------------------

  /**
   * <pre>
                +--------------------+
                | AbstractAddressDo  |
                +---------^----------+
                          |
              +-----------+--------------+
              |                          |
     +--------+-----------+   +----------+---------+
     |ElectronicAddressDo |   | PhysicalAddressDo  |
     +--------------------+   +----------+---------+
                                         |
                              +----------+---------+
                              |PhysicalAddressExDo |  (@Replace)
                              +--------------------+
   * </pre>
   */
  @Test
  public void testDeserialize_TestPersonDoWithReplacedPhysicalAddress() throws Exception {
    IBean<?> registeredBean = TestingUtility.registerBean(new BeanMetaData(TestPhysicalAddressExDo.class).withReplace(true));
    try {
      String input = readResourceAsString("TestPersonDo.json");
      TestPersonDo personDo = s_dataObjectMapper.readValue(input, TestPersonDo.class);
      assertEquals(personDo.getAddresses().get(0).getClass(), TestElectronicAddressDo.class);
      assertEquals(personDo.getAddresses().get(1).getClass(), TestPhysicalAddressExDo.class);
      assertEquals(personDo.getAddresses().get(2).getClass(), TestPhysicalAddressExDo.class);
    }
    finally {
      TestingUtility.unregisterBean(registeredBean);
    }
  }

  /**
   * Test case 1: Core DO replaced with Project DO, keeping the assigned core TypeName
   *
   * <pre>
    +--------------------+
    |  TestCoreExampleDo |     (@TypeName("TestCoreExample"))
    +---------+----------+
              |
    +--------------------+
    |TestProjectExampleDo|    (@Replace)
    +--------------------+
   * </pre>
   */
  @Test
  public void testSerializeDeserialize_ReplacedCoreDo() throws Exception {
    TestCoreExample1Do coreDo = BEANS.get(TestCoreExample1Do.class);
    coreDo.withName("core-name1");
    String json = s_dataObjectMapper.writeValueAsString(coreDo);
    String expectedJson = readResourceAsString("TestCoreExample1Do.json");
    s_testHelper.assertJsonEquals(expectedJson, json);

    TestCoreExample1Do coreDoMarshalled = s_dataObjectMapper.readValue(expectedJson, TestCoreExample1Do.class);
    s_testHelper.assertDoEntityEquals(coreDo, coreDoMarshalled);
  }

  @Test
  public void testSerializeDeserialize_ProjectDo() throws Exception {
    TestProjectExample1Do projectDo = BEANS.get(TestProjectExample1Do.class);
    projectDo.withName("core-name1");
    projectDo.withNameEx("project-name1");
    String json = s_dataObjectMapper.writeValueAsString(projectDo);
    String expectedJson = readResourceAsString("TestProjectExample1Do.json");
    s_testHelper.assertJsonEquals(expectedJson, json);

    TestProjectExample1Do coreDoMarshalled = s_dataObjectMapper.readValue(json, TestProjectExample1Do.class);
    s_testHelper.assertDoEntityEquals(projectDo, coreDoMarshalled);
  }

  /**
   * Test case 2: Core DO replaced with Project DO, changing the assigned core TypeName
   *
   * <pre>
    +---------------------+
    |  TestCoreExample2Do |     (@TypeName("TestCoreExample2"))
    +---------+-----------+
              |
    +---------------------+
    |TestProjectExample2Do|    (@Replace), (@TypeName("TestProjectExample2"))
    +---------------------+
   * </pre>
   */
  @Test
  public void testSerializeDeserialize_ReplacedCore2Do() throws Exception {
    TestCoreExample2Do coreDo = BEANS.get(TestCoreExample2Do.class);
    coreDo.withName("core-name2");
    String json = s_dataObjectMapper.writeValueAsString(coreDo);
    String expectedJson = readResourceAsString("TestCoreExample2Do.json");
    s_testHelper.assertJsonEquals(expectedJson, json);

    TestCoreExample2Do coreDoMarshalled = s_dataObjectMapper.readValue(expectedJson, TestCoreExample2Do.class);
    s_testHelper.assertDoEntityEquals(coreDo, coreDoMarshalled);
  }

  @Test
  public void testSerializeDeserialize_Project2Do() throws Exception {
    TestProjectExample2Do projectDo = BEANS.get(TestProjectExample2Do.class);
    projectDo.withName("core-name2");
    projectDo.withNameEx("project-name2");
    String json = s_dataObjectMapper.writeValueAsString(projectDo);
    String expectedJson = readResourceAsString("TestProjectExample2Do.json");
    s_testHelper.assertJsonEquals(expectedJson, json);

    TestProjectExample2Do coreDoMarshalled = s_dataObjectMapper.readValue(json, TestProjectExample2Do.class);
    s_testHelper.assertDoEntityEquals(projectDo, coreDoMarshalled);
  }

  /**
   * Test case 3: Core DO extended in Project DO, changing the assigned core TypeName
   *
   * <pre>
    +---------------------+
    |  TestCoreExample3Do |     (@TypeName("TestCoreExample3"))
    +---------+-----------+
              |
    +---------------------+
    |TestProjectExample3Do|    (@TypeName("TestProjectExample3"))
    +---------------------+
   * </pre>
   */
  @Test
  public void testSerializeDeserialize_ExtendedCore3Do() throws Exception {
    TestCoreExample3Do coreDo = BEANS.get(TestCoreExample3Do.class);
    coreDo.withName("core-name3");
    String json = s_dataObjectMapper.writeValueAsString(coreDo);
    String expectedJson = readResourceAsString("TestCoreExample3Do.json");
    s_testHelper.assertJsonEquals(expectedJson, json);

    TestCoreExample3Do coreDoMarshalled = s_dataObjectMapper.readValue(expectedJson, TestCoreExample3Do.class);
    s_testHelper.assertDoEntityEquals(coreDo, coreDoMarshalled);
  }

  @Test
  public void testSerializeDeserialize_Project3Do() throws Exception {
    TestProjectExample3Do projectDo = BEANS.get(TestProjectExample3Do.class);
    projectDo.withName("core-name3");
    projectDo.withNameEx("project-name3");
    String json = s_dataObjectMapper.writeValueAsString(projectDo);
    String expectedJson = readResourceAsString("TestProjectExample3Do.json");
    s_testHelper.assertJsonEquals(expectedJson, json);

    TestProjectExample3Do coreDoMarshalled = s_dataObjectMapper.readValue(json, TestProjectExample3Do.class);
    s_testHelper.assertDoEntityEquals(projectDo, coreDoMarshalled);
  }

  // ------------------------------------ exception handling tests ------------------------------------

  @Test(expected = JsonMappingException.class)
  public void testMappingException() throws Exception {
    s_dataObjectMapper.readValue("[]", DoEntity.class);
  }

  @Test(expected = JsonParseException.class)
  public void testParseException() throws Exception {
    s_dataObjectMapper.readValue("[", DoEntity.class);
  }

  // ------------------------------------ performance tests ------------------------------------

  @Test
  public void testGeneratedLargeJsonObject() throws Exception {
    DoEntity entity = new DoEntity();
    // generate some complex, random JSON structure
    for (int i = 0; i < 1000; i++) {
      switch (i % 5) {
        case 0:
          entity.put("attribute" + i, createTestDo());
          break;
        case 1:
          entity.put("attribute" + i, createTestCollectionsDo());
          break;
        case 2:
          entity.put("attribute" + i, createTestPersonDo());
          break;
        case 3:
          entity.put("attribute" + i, createTestItemDo("id" + i, "value" + i));
          break;
        case 4:
          entity.putNode("attribute" + i, createDoValue("simple-value" + i));
          break;
      }
    }
    String json = s_dataObjectMapper.writeValueAsString(entity);
    DoEntity marshalled = s_dataObjectMapper.readValue(json, DoEntity.class);
    s_testHelper.assertDoEntityEquals(entity, marshalled);
  }

  // ------------------------------------ common test helper methods ------------------------------------

  protected TestComplexEntityDo createTestDo() {
    TestComplexEntityDo testDo = BEANS.get(TestComplexEntityDo.class);
    testDo.id().set("4d2abc01-afc0-49f2-9eee-a99878d49728");
    testDo.stringAttribute().set("foo");
    testDo.integerAttribute().set(42);
    testDo.longAttribute().set(123l);
    testDo.floatAttribute().set(12.34f);
    testDo.doubleAttribute().set(56.78);
    testDo.bigDecimalAttribute().set(new BigDecimal("1.23456789"));
    testDo.bigIntegerAttribute().set(new BigInteger("123456789"));
    testDo.dateAttribute().set(DATE);
    testDo.objectAttribute().set(new String("fooObject"));
    testDo.withUuidAttribute(UUID.fromString("298d64f9-821d-49fe-91fb-6fb9860d4950"));
    testDo.withLocaleAttribute(Locale.forLanguageTag("de-CH"));

    List<TestItemDo> list = new ArrayList<>();
    list.add(BEANS.get(TestItemDo.class).withId("1234-1"));
    list.add(BEANS.get(TestItemDo.class).withId("1234-2"));
    testDo.itemsAttribute().set(list);

    testDo.itemAttribute().set(BEANS.get(TestItemDo.class).withId("1234-3").withStringAttribute("bar"));
    testDo.stringListAttribute().set(Arrays.asList("foo", "bar"));
    return testDo;
  }

  protected TestComplexEntityPojo createTestPoJo() {
    TestComplexEntityPojo testPoJo = new TestComplexEntityPojo();
    testPoJo.setId("4d2abc01-afc0-49f2-9eee-a99878d49728");
    testPoJo.setStringAttribute("foo");
    testPoJo.setIntegerAttribute(42);
    testPoJo.setLongAttribute(123l);
    testPoJo.setFloatAttribute(12.34f);
    testPoJo.setDoubleAttribute(56.78);
    testPoJo.setBigDecimalAttribute(new BigDecimal("1.23456789"));
    testPoJo.setBigIntegerAttribute(new BigInteger("123456789"));
    testPoJo.setDateAttribute(DATE);
    testPoJo.setObjectAttribute(new String("fooObject"));
    testPoJo.setUuidAttribute(UUID.fromString("298d64f9-821d-49fe-91fb-6fb9860d4950"));
    testPoJo.setLocaleAttribute(Locale.forLanguageTag("de-CH"));

    testPoJo.setItemsAttribute(new ArrayList<TestItemPojo>());
    TestItemPojo testItemPoJo1 = new TestItemPojo();
    testItemPoJo1.setId("1234-1");
    testPoJo.getItemsAttribute().add(testItemPoJo1);

    TestItemPojo testItemPoJo2 = new TestItemPojo();
    testItemPoJo2.setId("1234-2");
    testPoJo.getItemsAttribute().add(testItemPoJo2);

    TestItemPojo testItemPoJo3 = new TestItemPojo();
    testItemPoJo3.setId("1234-3");
    testItemPoJo3.setStringAttribute("bar");
    testPoJo.setItemAttribute(testItemPoJo3);

    List<String> stringList = Arrays.asList("foo", "bar");
    testPoJo.setStringListAttribute(stringList);

    return testPoJo;
  }

  protected TestItemDo createTestItemDo(String id, String attribute) {
    return BEANS.get(TestItemDo.class).withId(id).withStringAttribute(attribute);
  }

  protected TestItemPojo createTestItemPojo(String id, String attribute) {
    TestItemPojo testPojo = new TestItemPojo();
    testPojo.setId(id);
    testPojo.setStringAttribute(attribute);
    return testPojo;
  }

  protected TestItemPojo2 createTestItemPojo2(String id, String attribute) {
    TestItemPojo2 testPojo = new TestItemPojo2();
    testPojo.setId(id);
    testPojo.setStringAttribute(attribute);
    return testPojo;
  }

  protected <T> DoValue<T> createDoValue(T value) {
    DoValue<T> wrapper = new DoValue<>();
    wrapper.set(value);
    return wrapper;
  }

  protected void assertJsonResourceEquals(String expectedResourceName, String actual) {
    s_testHelper.assertJsonResourceEquals(JsonDataObjectsSerializationTest.class.getResource(expectedResourceName), actual);
  }

  protected String readResourceAsString(String resourceName) throws IOException {
    return s_testHelper.readResourceAsString(JsonDataObjectsSerializationTest.class.getResource(resourceName));
  }
}
