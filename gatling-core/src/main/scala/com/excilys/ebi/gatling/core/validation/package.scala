/**
 * Copyright 2011-2013 eBusiness Information, Groupe Excilys (www.excilys.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.excilys.ebi.gatling.core

package object validation {

	implicit class SuccessWrapper[T](value: T) {
		def success: Validation[T] = Success(value)
	}

	implicit class FailureWrapper(message: String) {
		def failure[T]: Validation[T] = Failure(message)
	}

	implicit class ValidationList[T](validations: List[Validation[T]]) {
		def sequence: Validation[List[T]] = {

			def sequenceRec(validations: List[Validation[T]]): Validation[List[T]] = validations match {
				case Nil => List.empty[T].success
				case head :: tail => head match {
					case Success(entry) => sequenceRec(tail).map(entry :: _)
					case Failure(message) => message.failure
				}
			}
			sequenceRec(validations)

		}
	}

}