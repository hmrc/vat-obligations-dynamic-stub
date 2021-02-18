/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package utils

import com.github.fge.jsonschema.main.JsonSchema
import mocks.MockSchemaRepository
import models.SchemaModel
import play.api.libs.json.{JsValue, Json}

class SchemaValidationSpec extends MockSchemaRepository {

  val validation = new SchemaValidation(mockSchemaRepository)

  val schema: JsValue = Json.obj(
    "title" -> "Person",
    "type" -> "object",
    "properties" -> Json.obj(
      "firstName" -> Json.obj(
        "type" -> "string"
      ),
      "lastName" -> Json.obj(
        "type" -> "string"
      )
    ),
    "required" -> Json.arr("firstName", "lastName")
  )

  "Calling .loadResponseSchema" should {

    "with a matching schema in mongo" should {

      "return a json schema" in {
        mockFindSchema(List(SchemaModel("testSchema","/test","GET", responseSchema = schema)))
        lazy val result = validation.loadResponseSchema("testSchema")

        await(result).isInstanceOf[JsonSchema]
      }
    }

    "without a matching schema in mongo" should {

      "throw an exception" in {
        mockFindSchema(List())

        val ex = intercept[Exception] {
          await(validation.loadResponseSchema("testSchema"))
        }
        ex.getMessage shouldEqual "Schema could not be retrieved/found in MongoDB"
      }
    }
  }

  "Calling .validateResponseJson" should {

    "with a valid json body" should {

      "return true" in {
        mockFindSchema(List(SchemaModel("testSchema","/test","GET", responseSchema = schema)))
        val json = Json.parse("""{ "firstName" : "Bob", "lastName" : "Bobson" }""")
        val result = validation.validateResponseJson("testSchema", Some(json))

        await(result) shouldEqual true
      }
    }

    "with an invalid json body" should {

      "return false" in {
        mockFindSchema(List(SchemaModel("testSchema","/test","GET", responseSchema = schema)))
        val json = Json.parse("""{ "firstName" : "Bob" }""")
        lazy val result = validation.validateResponseJson("testSchema", Some(json))

        await(result) shouldEqual false
      }
    }
  }

  "Calling .loadUrlRegex" should {

    "return the url of the SchemaModel" in {
      mockFindSchema(List(SchemaModel("testSchema","/test","GET", responseSchema = schema)))
      lazy val result = validation.loadUrlRegex("testSchema")

      await(result) shouldEqual "/test"
    }
  }

  "Calling .validateUrlMatch" should {

    "return 'true' if the urls match" in {
      mockFindSchema(List(SchemaModel("testSchema", "/test", "GET", responseSchema = schema)))
      lazy val result = validation.validateUrlMatch("testSchema", "/test")

      await(result) shouldEqual true
    }
  }
}
