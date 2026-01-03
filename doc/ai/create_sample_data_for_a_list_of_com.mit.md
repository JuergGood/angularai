### Plan for creating sample data and tests for a list of `Involved` with polymorphic `contact`

#### 1) Inspect and confirm model and current Gson setup
- Verify that `Involved#contact` is of type `Contact` and that `Person` and `Company` extend `Contact` (already confirmed from generated sources).
- Review current Gson configuration (`ContactJson` / any central parser) and confirm `XMLGregorianCalendar` adapter is registered and working.

#### 2) Add polymorphic JSON (de)serialization for `Contact`
- Implement a Gson `TypeAdapter` for `Contact` that uses a discriminator field in JSON (e.g., `"contactType"`: `"person"|"company"`).
- Deserialization: when parsing `Involved`, if `contact.contactType == "person"` → deserialize to `Person`; if `"company"` → `Company`.
- Serialization: when emitting `Involved`, include `contactType` in the nested `contact` object accordingly.
- Register this adapter in the central Gson builder (`ContactJson.gson()`), keeping the existing `XMLGregorianCalendarTypeAdapter`.

#### 3) Create sample JSON data for a list of `Involved`
- Add `src/test/resources/json/involved.list.sample.json` containing two entries:
  - Entry 1: `Involved` with `contact` of type `Person` (with typical `Person` fields plus inherited `Contact` fields like `uniqueKey`, `createdOn`, `modifiedOn`).
  - Entry 2: `Involved` with `contact` of type `Company` (with `Company` fields plus inherited ones).
- Use ISO strings for datetime fields so they parse via the existing `XMLGregorianCalendar` adapter.

#### 4) Implement unit test for parsing the list of `Involved`
- Add `src/test/java/.../gson/InvolvedListJsonTest.java`.
- Use `ContactJson.gson()` and `TypeToken<List<Involved>>` to parse the JSON list.
- Assertions:
  - List size is 2.
  - First item `contact` is `instanceof Person` with expected field values.
  - Second item `contact` is `instanceof Company` with expected field values.
  - Verify `createdOn`/`modifiedOn` via `toXMLFormat()`.

#### 5) Run tests and adjust if needed
- Run the new test and existing `CompanyJsonTest` and `PersonJsonTest` to ensure no regressions.
- If any conflicts in Gson configuration occur, reconcile in the central builder.

#### 6) Deliverables
- New adapter class for polymorphic `Contact` handling.
- Sample JSON file with a list of two `Involved` objects.
- A JUnit test class that validates parsing and fields thoroughly.

If you prefer a different discriminator name or want to avoid adding a `contactType` field (e.g., infer by presence of `firstName` vs `name`), let me know and I’ll adjust the adapter and samples accordingly.