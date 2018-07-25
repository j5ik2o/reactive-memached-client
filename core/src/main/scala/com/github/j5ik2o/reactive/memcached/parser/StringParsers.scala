package com.github.j5ik2o.reactive.memcached.parser

import com.github.j5ik2o.reactive.memcached.parser.model._
import fastparse.all._

object StringParsers {

  val digit: P0      = P(CharIn('0' to '9'))
  val lowerAlpha: P0 = P(CharIn('a' to 'z'))
  val upperAlpha: P0 = P(CharIn('A' to 'Z'))
  val alpha: P0      = P(lowerAlpha | upperAlpha)
  val alphaDigit: P0 = P(alpha | digit)
  val crlf: P0       = P("\r\n")

  val error: P[ErrorExpr.type]        = P("ERROR" ~ crlf).map(_ => ErrorExpr)
  val clientError: P[ClientErrorExpr] = P("CLIENT_ERROR" ~ (!crlf ~/ AnyChar).rep(1).! ~ crlf).map(ClientErrorExpr)
  val serverError: P[ServerErrorExpr] = P("SERVER_ERROR" ~ (!crlf ~/ AnyChar).rep(1).! ~ crlf).map(ServerErrorExpr)
  val allErrors: P[Expr]              = P(error | clientError | serverError)

  val end: P[EndExpr.type]             = P("END" ~ crlf).map(_ => EndExpr)
  val deleted: P[DeletedExpr.type]     = P("DELETED" ~ crlf).map(_ => DeletedExpr)
  val stored: P[StoredExpr.type]       = P("STORED" ~ crlf).map(_ => StoredExpr)
  val notStored: P[NotStoredExpr.type] = P("NOT_STORED" ~ crlf).map(_ => NotStoredExpr)
  val exists: P[ExistsExpr.type]       = P("EXISTS" ~ crlf).map(_ => ExistsExpr)
  val notFound: P[NotFoundExpr.type]   = P("NOT_FOUND" ~ crlf).map(_ => NotFoundExpr)

  val key: P[String]      = alphaDigit.rep(1).!
  val flags: P[Int]       = digit.rep(1).!.map(_.toInt)
  val expireTime: P[Long] = digit.rep(1).!.map(_.toLong)

  val storageCommandResponse: P[Expr] = P((stored | notStored | exists | notFound) | allErrors)

  val value = P("VALUE" ~ key ~ flags ~ expireTime ~ crlf ~ (!end ~/ AnyChar).rep.! ~ end).map {
    case (key, flags, expireTime, value, _) =>
      ValueExpr(key, flags, expireTime, value)
  }
  val retrievalCommandResponse: P[Expr] = P(end | value | allErrors)

  val deletionCommandResponse: P[Expr] = P(deleted | notFound | allErrors)

}