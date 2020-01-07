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

import models.DataModel
import org.scalatest.BeforeAndAfter
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.mvc.Http.Status
import testUtils.TestSupport

class RequestHandlerControllerSpec extends TestSupport with BeforeAndAfter {

  before {
    dropTestCollection("vat-obligations-dynamic-stub")
  }

  object TestRequestHandlerController extends RequestHandlerController(repo, cc)

  lazy val successModel = DataModel(
    _id = "test",
    method = "GET",
    status = Status.OK,
    response = None
  )

  lazy val successWithBodyModel = DataModel(
    _id = "bodyTest",
    method = "GET",
    status = Status.OK,
    response = Some(Json.parse("""{"something" : "hello"}"""))
  )

  "The getRequestHandler method" should {

    "return the status code specified in the model" in {
      repo.insert(successModel)
      lazy val result = TestRequestHandlerController.getRequestHandler("")(FakeRequest("GET", "test"))
      status(result) shouldBe Status.OK
    }

    "return the status and body" in {
      repo.insert(successWithBodyModel)
      lazy val result = TestRequestHandlerController.getRequestHandler("")(FakeRequest("GET", "bodyTest"))
      status(result) shouldBe Status.OK
      await(bodyOf(result)) shouldBe s"${successWithBodyModel.response.get}"
    }

    "return a 404 status when the endpoint cannot be found" in {
      lazy val result = TestRequestHandlerController.getRequestHandler("")(FakeRequest())
      status(result) shouldBe Status.NOT_FOUND
    }
  }

  "The postRequestHandler method" should {

    "return the corresponding response of an incoming POST request" in {
      repo.insert(successWithBodyModel)
      lazy val result = TestRequestHandlerController.postRequestHandler("/test")(FakeRequest("POST", "bodyTest"))
      await(bodyOf(result)) shouldBe s"${successWithBodyModel.response.get}"
    }

    "return a response status when there is no stubbed response body for an incoming POST request" in {
      repo.insert(successModel)
      lazy val result = TestRequestHandlerController.postRequestHandler("/test")(FakeRequest("POST", "test"))
      status(result) shouldBe Status.OK
    }

    "return a 404 status if the endpoint specified in the POST request can't be found" in {
      lazy val result = TestRequestHandlerController.postRequestHandler("/test")(FakeRequest())
      status(result) shouldBe Status.NOT_FOUND
    }
  }

  "Calling .errorResponseBody" should {

    "return a formatted json body" in {
      val body = Json.obj(
        "status" -> "404",
        "message" -> s"Could not find endpoint in Dynamic Stub matching the URI: url",
        "path" -> "url"
      )
      lazy val result = TestRequestHandlerController.errorResponseBody("url")

      result shouldBe body
    }
  }

}