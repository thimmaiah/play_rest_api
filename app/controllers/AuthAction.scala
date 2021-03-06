package controllers

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success

import javax.inject.Inject
import play.api.mvc._
import play.api.mvc.Results.Forbidden
import play.api.mvc.WrappedRequest
import play.api.Logger

import user._

case class UserRequest[A](user: User, request: Request[A])
    extends WrappedRequest(request)

object AuthAction {
  
  /**
   *  This action is used to extract the jwt token and enrich the request with the User 
  */
  class JWTAction @Inject()(
      val parser: BodyParsers.Default,
      jwtUtils: JwtUtils,
      userRepo: UserRepository)(implicit val executionContext: ExecutionContext)
      extends ActionBuilder[UserRequest, AnyContent]
      with ActionRefiner[Request, UserRequest] {

    private val logger: Logger = Logger(this.getClass())

    def refine[A](request: Request[A]) = {
     
      val jwtToken = request.headers.get("authorization").getOrElse("")

      if (jwtUtils.isValidToken(jwtToken)) {
        jwtUtils.decodePayload(jwtToken) match {
          case Success(json) => {
            logger.debug(s"Got jwtToken $json")
            userRepo.find((json \ "sub").as[String].toLong).map { u =>

              logger.debug(s"Got current user $u")
              u match {
                case Some(user) => Right(new UserRequest(user, request))
                case _          => Left(Forbidden)
              }
            }
          }
          case Failure(_) => Future.successful(Left(Forbidden))
        }
      } else
        Future.successful(Left(Forbidden))

    }

  }

}
