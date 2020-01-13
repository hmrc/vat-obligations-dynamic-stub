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

package mocks

import models.DataModel
import reactivemongo.api.commands.{UpdateWriteResult, WriteError, WriteResult}
import repositories.DataRepository
import testUtils.TestSupport
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito.when
import org.mockito.ArgumentMatchers.any
import org.mockito.stubbing.OngoingStubbing

import scala.concurrent.Future

trait MockDataRepository extends TestSupport with MockitoSugar {

  val successWriteResult = UpdateWriteResult(ok = true, 1, 1, Seq(), Seq(), None, None, None)
  val errorWriteResult =
    UpdateWriteResult(ok = false, 1, 0, Seq(), writeErrors = Seq(WriteError(1,1,"Error")), None, None, None)

  lazy val mockDataRepository: DataRepository = mock[DataRepository]

  def mockAddEntry(document: DataModel)(response: WriteResult): OngoingStubbing[Future[WriteResult]] =
    when(mockDataRepository.insert(document)(ec)).thenReturn(response)

  def mockRemoveById(id: String)(response: WriteResult): OngoingStubbing[Future[WriteResult]] =
    when(mockDataRepository.removeById(id)(ec)).thenReturn(response)

  def mockRemoveAll(response: WriteResult): OngoingStubbing[Future[WriteResult]] =
    when(mockDataRepository.removeAll()(ec)).thenReturn(response)

  def mockFind(response: List[DataModel]): OngoingStubbing[Future[List[DataModel]]] =
    when(mockDataRepository.find(any())(any())).thenReturn(response)
}
