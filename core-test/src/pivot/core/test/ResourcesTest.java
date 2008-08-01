package pivot.core.test;

import junit.framework.TestCase;

import pivot.collections.List;
import pivot.collections.Map;
import pivot.serialization.SerializationException;
import pivot.util.Resources;

public class ResourcesTest extends TestCase {

	public void testReadDefaultLocale() throws Exception {

		Resources res = new Resources("resources/test1.json");
		assertResources(res, "SGML", "Standard Generalized Markup Language");
	}

	/**
	 * The resource overrides the term for the country.
	 *
	 * @throws Exception
	 */
	public void testRead_GB_Locale() throws Exception {
		Resources res = new Resources("resources/test2.json");
		assertResources(res, "SGML",
				"How Do, Youth, Standard Generalized Markup Language");

	}

	/**
	 * The resource overrides the term for the country and the acronym for the
	 * language.
	 *
	 * @throws Exception
	 */
	public void testRead_GB_en_Locale() throws Exception {
		Resources res = new Resources("resources/test3.json");
		assertResources(res, "XSGML",
				"How Do, Youth, Standard Generalized Markup Language");

	}

	/**
	 * The resource overrides the term and the acronym for the country.
	 *
	 * @throws Exception
	 */
	public void testRead_GB_en_LocaleExtraOverride() throws Exception {
		Resources res = new Resources("resources/test6.json");
		assertResources(res, "XSGML",
				"eXtra Standard Generalized Markup Language");

	}

	public void testSerialisationException() throws Exception {

		try {
			new Resources("resources/test4.json");
			fail("Expected SerialisationException");
		} catch (SerializationException e) {
		}

	}

	public void testIllegalArgumentException() throws Exception {

		// resource doesn't exist...
		try {
			new Resources("resources/test5.json");
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}

		// resource exists, but locale is null
		try {
			new Resources("resources/test1.json", null);
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}

	}

	@SuppressWarnings("unchecked")
	private static void assertResources(Resources res, String acronym,
			String term) {
		assertTrue(res.containsKey("glossary"));

		Map<String, Object> glossary = (Map<String, Object>) res
				.get("glossary");
		assertNotNull(glossary);
		assertTrue(glossary.containsKey("GlossDiv"));

		Map<String, Object> glossDiv = (Map<String, Object>) glossary
				.get("GlossDiv");
		assertNotNull(glossDiv);

		assertEquals("S", glossDiv.get("title"));

		assertTrue(glossDiv.containsKey("GlossList"));
		Map<String, Object> glossList = (Map<String, Object>) glossDiv
				.get("GlossList");
		assertNotNull(glossList);

		assertTrue(glossList.containsKey("GlossEntry"));
		Map<String, Object> glossEntry = (Map<String, Object>) glossList
				.get("GlossEntry");
		assertNotNull(glossEntry);

		assertEquals(acronym, glossEntry.get("Acronym"));
		assertEquals(term, glossEntry.get("GlossTerm"));

		assertTrue(glossEntry.containsKey("GlossDef"));
		Map<String, Object> glossDef = (Map<String, Object>) glossEntry
				.get("GlossDef");
		assertNotNull(glossDef);

		assertTrue(glossDef.containsKey("GlossSeeAlso"));
		List<String> list = (List<String>) glossDef.get("GlossSeeAlso");
		assertNotNull(list);
		assertEquals(2, list.getLength());
	}

}
