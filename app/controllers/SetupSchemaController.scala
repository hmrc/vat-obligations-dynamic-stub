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

package controllers

import javax.inject.{Inject, Singleton}
import models.SchemaModel
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import repositories.SchemaRepository
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.ExecutionContext

@Singleton
class SetupSchemaController @Inject()(schemaRepository: SchemaRepository,
                                      cc: ControllerComponents)
                                     (implicit ec: ExecutionContext) extends BackendController(cc) {

  val addSchema: Action[JsValue] = Action.async(parse.json) { implicit request =>
    withJsonBody[SchemaModel](
      model =>
        schemaRepository.insert(model).map(result => if (result.ok) {
          Ok(s"Successfully added Schema: ${request.body}")
        } else {
          InternalServerError("Could not store data")
        })
    ).recover {
      case _ => BadRequest("Error Parsing Json SchemaModel")
    }
  }

  val removeSchema: String => Action[AnyContent] = id => Action.async {
    schemaRepository.removeById(id).map(result => if (result.ok) {
      Ok("Success")
    } else {
      InternalServerError("Could not delete data")
    })
  }

  val removeAll: Action[AnyContent] = Action.async {
    schemaRepository.removeAll().map(result => if (result.ok) {
      Ok("Removed All Schemas")
    } else {
      InternalServerError("Unexpected Error Clearing MongoDB.")
    })
  }
}
