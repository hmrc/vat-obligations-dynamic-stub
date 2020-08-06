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

import javax.inject.Inject
import models.DataModel
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.controller.BackendController
import models.HttpMethod._
import repositories.DataRepository
import utils.SchemaValidation

import scala.concurrent.{ExecutionContext, Future}

class SetupDataController @Inject()(dataRepository: DataRepository,
                                    schemaValidation: SchemaValidation,
                                    cc: ControllerComponents)
                                   (implicit ec: ExecutionContext) extends BackendController(cc) {

  val addData: Action[JsValue] = Action.async(parse.json) { implicit request =>
    withJsonBody[DataModel](
      model => model.method.toUpperCase match {
        case GET | POST =>
          schemaValidation.validateUrlMatch(model.schemaId, model._id) flatMap {
            case true =>
              schemaValidation.validateResponseJson(model.schemaId, model.response) flatMap {
                case true => addStubDataToDB(model)
                case false =>
                  Future.successful(BadRequest(
                  s"The Json Body:\n\n${model.response.get} did not validate against the Schema Definition"))
              }
            case false =>
              schemaValidation.loadUrlRegex(model.schemaId) map {
                regex => BadRequest(s"URL ${model._id} did not match the Schema Definition Regex $regex")
              }
          }
        case x =>
          Future.successful(BadRequest(s"The method: $x is currently unsupported"))
      }
    ).recover {
      case _ =>
        InternalServerError("Error Parsing Json DataModel")
    }
  }

  private def addStubDataToDB(model: DataModel): Future[Result] = {
    dataRepository.insert(model).map(result => if (result.ok) {
      Ok(s"The following JSON was added to the stub: \n\n${Json.toJson(model)}")
    } else {
      InternalServerError(s"Failed to add data to Stub.")
    })
  }

  val removeData: String => Action[AnyContent] = url => Action.async { implicit request =>
    dataRepository.removeById(url).map {
      case result if result.ok => Ok("Success")
      case _ => InternalServerError("Could not delete data")
    }
  }

  val removeAll: Action[AnyContent] = Action.async { implicit request =>
    dataRepository.removeAll().map {
      case result if result.ok => Ok("Removed All Stubbed Data")
      case _ => InternalServerError("Unexpected Error Clearing MongoDB.")
    }
  }

}
