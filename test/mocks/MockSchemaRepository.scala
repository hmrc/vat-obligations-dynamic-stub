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

package mocks

import models.SchemaModel
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import org.scalatestplus.mockito.MockitoSugar
import reactivemongo.api.commands.{UpdateWriteResult, WriteError, WriteResult}
import repositories.SchemaRepository
import testUtils.TestSupport

import scala.concurrent.Future

trait MockSchemaRepository extends TestSupport with MockitoSugar {

  val successWriteResult: UpdateWriteResult =
    UpdateWriteResult(ok = true, 1, 1, Seq(), Seq(), None, None, None)
  val errorWriteResult: UpdateWriteResult =
    UpdateWriteResult(ok = false, 1, 0, Seq(), Seq(WriteError(1,1,"Error")), None, None, None)

  lazy val mockSchemaRepository: SchemaRepository = mock[SchemaRepository]

  def mockAddSchema(model: SchemaModel)(response: Future[WriteResult]): OngoingStubbing[Future[WriteResult]] =
    when(mockSchemaRepository.insert(ArgumentMatchers.any()))
      .thenReturn(response)

  def mockRemoveSchema(id: String)(response: WriteResult): OngoingStubbing[Future[WriteResult]] =
    when(mockSchemaRepository.removeById(ArgumentMatchers.eq(id)))
      .thenReturn(Future.successful(response))

  def mockRemoveAllSchemas(response: WriteResult): OngoingStubbing[Future[WriteResult]] =
    when(mockSchemaRepository.removeAll())
      .thenReturn(Future.successful(response))

  def mockFindSchema(response: List[SchemaModel]): OngoingStubbing[Future[List[SchemaModel]]] =
    when(mockSchemaRepository.find(ArgumentMatchers.any())).thenReturn(Future.successful(response))
}
