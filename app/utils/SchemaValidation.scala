/*
 * Copyright 2020 HM Revenue & Customs
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

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.github.fge.jsonschema.main.{JsonSchema, JsonSchemaFactory}
import javax.inject.{Inject, Singleton}
import play.api.libs.json.JsValue
import repositories.SchemaRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SchemaValidation @Inject()(repository: SchemaRepository)(implicit ec: ExecutionContext) {

  private final lazy val jsonMapper = new ObjectMapper()
  private final lazy val jsonFactory = jsonMapper.getFactory

  def loadResponseSchema(schemaId: String): Future[JsonSchema] = {
    val schemaMapper = new ObjectMapper()
    val factory = schemaMapper.getFactory
    repository.find("_id" -> schemaId).map {
      case Nil => throw new Exception("Schema could not be retrieved/found in MongoDB")
      case head :: _ =>
        val schemaParser: JsonParser = factory.createParser(head.responseSchema.toString)
        val schemaJson: JsonNode = schemaMapper.readTree(schemaParser)
        JsonSchemaFactory.byDefault().getJsonSchema(schemaJson)
    }
  }

  def validateResponseJson(schemaId: String, json: Option[JsValue]): Future[Boolean] =
    json.fold(Future.successful(true)) { response =>
      loadResponseSchema(schemaId).map { schema =>
        val jsonParser = jsonFactory.createParser(response.toString)
        val jsonNode: JsonNode = jsonMapper.readTree(jsonParser)
        schema.validate(jsonNode).isSuccess
      }
    }

  def loadUrlRegex(schemaId: String): Future[String] = repository.find("_id" -> schemaId).map {
    case Nil => throw new Exception("Schema could not be retrieved/found in MongoDB")
    case head :: _ => head.url
  }

  def validateUrlMatch(schemaId: String, url: String): Future[Boolean] =
    loadUrlRegex(schemaId).map(regex => url.matches(regex))
}
