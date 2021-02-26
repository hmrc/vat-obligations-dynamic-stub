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

import mocks.{MockDataRepository, MockSchemaValidation}
import models.DataModel
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.mvc.Http.Status

import scala.concurrent.Future

class SetupDataControllerSpec extends MockDataRepository with MockSchemaValidation {

  object TestSetupDataController extends SetupDataController(mockDataRepository, mockSchemaValidation, cc)

  "SetupDataController.addData" when {

    "validateUrlMatch returns 'true'" should {
      val model: DataModel = DataModel(
        _id = "1234",
        schemaId = "2345",
        method = "GET",
        response = Some(Json.parse("{}")),
        status = Status.OK)

      "when validateResponseJson returns 'true'" should {


        "return Status OK (200) if data successfully added to stub" in {
          lazy val request = FakeRequest().withBody(Json.toJson(model)).withHeaders(("Content-Type", "application/json"))
          lazy val result = TestSetupDataController.addData(request)

          mockValidateUrlMatch("2345", "1234")(response = true)
          mockValidateResponseJson("2345", Some(Json.parse("{}")))(response = Future.successful(true))
          mockAddEntry(model)(successWriteResult)
          status(result) shouldBe Status.OK
        }
        "return Status InternalServerError (500) if unable to add data to the stub" in {
          lazy val request = FakeRequest().withBody(Json.toJson(model)).withHeaders(("Content-Type", "application/json"))
          lazy val result = TestSetupDataController.addData(request)

          mockValidateUrlMatch("2345", "1234")(response = true)
          mockValidateResponseJson("2345", Some(Json.parse("{}")))(response = Future.successful(true))
          mockAddEntry(model)(errorWriteResult)
          status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        }

      }

      "return Status BadRequest (400) when validateResponseJson returns 'false'" in {
        lazy val request = FakeRequest().withBody(Json.toJson(model)).withHeaders(("Content-Type", "application/json"))
        lazy val result = TestSetupDataController.addData(request)

        mockValidateUrlMatch("2345", "1234")(response = true)
        mockValidateResponseJson("2345", Some(Json.parse("""{}""")))(response = Future.successful(false))
        status(result) shouldBe Status.BAD_REQUEST
      }

      "return Status InternalServerError (500) when validateResponseJson returns 'false'" in {
        lazy val request = FakeRequest().withBody(Json.toJson(model)).withHeaders(("Content-Type", "application/json"))
        lazy val result = TestSetupDataController.addData(request)

        mockValidateUrlMatch("2345", "1234")(response = true)
        mockValidateResponseJson("2345", Some(Json.parse("""{}""")))(response = Future.failed(new Exception("f")))
        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      }
    }

    "validateUrlMatch returns 'false'" should {

      val model: DataModel = DataModel(
        _id = "1234",
        schemaId = "2345",
        method = "GET",
        response = Some(Json.parse("{}")),
        status = Status.OK)

      "return Status BadRequest (400)" in {
        lazy val request = FakeRequest().withBody(Json.toJson(model)).withHeaders(("Content-Type", "application/json"))
        lazy val result = TestSetupDataController.addData(request)

        mockValidateUrlMatch("2345", "1234")(response = false)
        mockLoadUrlRegex("2345")(response = "w")
        status(result) shouldBe Status.BAD_REQUEST
      }

    }

    "not a GET request" should {

      val model: DataModel = DataModel(
        _id = "1234",
        schemaId = "2345",
        method = "BLOB",
        response = Some(Json.parse("{}")),
        status = Status.OK
      )

      "return Status BadRequest (400)" in {
        lazy val request = FakeRequest().withBody(Json.toJson(model)).withHeaders(("Content-Type", "application/json"))
        lazy val result = TestSetupDataController.addData(request)

        status(result) shouldBe Status.BAD_REQUEST
      }
    }
  }

  "SetupDataController.removeData" should {

    "return Status OK (200) on successful removal of data from the stub" in {
      lazy val result = TestSetupDataController.removeData("someID")(fakeRequest)
      mockRemoveById("someID")(successWriteResult)

      status(result) shouldBe Status.OK
    }

    "return Status InternalServerError (500) on unsuccessful removal of data from the stub" in {
      lazy val result = TestSetupDataController.removeData("someID")(fakeRequest)
      mockRemoveById("someID")(errorWriteResult)

      status(result) shouldBe Status.INTERNAL_SERVER_ERROR
    }
  }

  "SetupDataController.removeAllData" should {

    "return Status OK (200) on successful removal of all stubbed data" in {
      lazy val result = TestSetupDataController.removeAll()(fakeRequest)
      mockRemoveAll(successWriteResult)

      status(result) shouldBe Status.OK
    }

    "return Status InternalServerError (500) on unsuccessful removal of all stubbed data" in {
      lazy val result = TestSetupDataController.removeAll()(fakeRequest)
      mockRemoveAll(errorWriteResult)

      status(result) shouldBe Status.INTERNAL_SERVER_ERROR
    }
  }
}
