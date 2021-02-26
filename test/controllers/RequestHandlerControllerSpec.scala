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

package controllers

import mocks.MockDataRepository
import models.DataModel
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.mvc.Http.Status
import testUtils.TestSupport

class RequestHandlerControllerSpec extends TestSupport with MockDataRepository {

  object TestRequestHandlerController extends RequestHandlerController(mockDataRepository, cc)

  lazy val successModel: DataModel = DataModel(
    _id = "/test",
    schemaId = "asdf",
    method = "GET",
    status = Status.OK,
    response = Some(Json.parse("""{"something" : "hello"}"""))
  )

  "The getRequestHandler method" when {

    "the record is found in the database" should {

      lazy val result = {
        mockFind(List(successModel))
        TestRequestHandlerController.getRequestHandler("/test")(FakeRequest())
      }

      "return the status code specified in the model" in {
        status(result) shouldBe Status.OK
      }

      "return the body specified in the model" in {
        await(bodyOf(result)) shouldBe s"${successModel.response.get}"
      }
    }

    "the record cannot be found in the database" should {

      lazy val result = {
        mockFind(List())
        TestRequestHandlerController.getRequestHandler("/test")(FakeRequest())
      }

      "return a 404 status" in {
        status(result) shouldBe Status.NOT_FOUND
      }

      "return the expected body when the endpoint cannot be found" in {
        await(bodyOf(result)) shouldBe s"${TestRequestHandlerController.errorResponseBody}"
      }
    }
  }
}
