/**
  * MIT License
  *
  * Copyright (c) 2018 Junichi Kato
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all
  * copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
package com.github.j5ik2o.reactive.memcached.command

import java.text.ParseException
import java.util.UUID

import com.github.j5ik2o.reactive.memcached.parser.model.Expr
import fastparse.core
import fastparse.core.Parsed
import scodec.bits.ByteVector

trait CommandRequest {
  type Elem
  type Repr
  type P[+T] = core.Parser[T, Elem, Repr]

  type Response <: CommandResponse

  type Handler = PartialFunction[(Expr, Int), (Response, Int)]

  val id: UUID
  val key: String
  val isMasterOnly: Boolean

  def asString: String

  protected def responseParser: P[Expr]

  protected def convertToParseSource(s: ByteVector): Repr

  def parse(text: ByteVector, index: Int = 0): Either[ParseException, (Response, Int)] = {
    responseParser.parse(convertToParseSource(text), index) match {
      case f @ Parsed.Failure(_, index, _) =>
        Left(new ParseException(f.msg, index))
      case Parsed.Success(value, index) => Right(parseResponse((value, index)))
    }
  }

  protected def parseResponse: Handler

}
