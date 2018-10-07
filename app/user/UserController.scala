package user

import javax.inject.Inject

import play.api.Logger
import play.api.data.Form
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class UserController @Inject()(cc: ControllerComponents, repo: UserRepository)(implicit ec: ExecutionContext) 
extends AbstractController(cc) {

    private val logger:Logger = Logger(this.getClass())
    import UserSerialization._

    def index = Action.async { request =>
        logger.info("index:")
        repo.list().map { users =>
            Ok(Json.toJson(users))
        }
    }

    def show(id:Long) = Action.async { request =>
        logger.info(s"show $id")
        repo.find(id).map { user =>
            logger.info(s"show $user")
            user match {
                case Some(u) => Ok(Json.toJson(u))
                case None => NotFound
            }
            
        }
        
    }

    def add = Action.async (parse.json[User]) { request =>
        val u:User = request.body
        repo.create(u).map { addedUser =>
            Created(Json.toJson(addedUser))
        }
    }

    def delete(id:Long) = Action.async { request =>
        logger.info(s"delete $id")
        repo.delete(id).map { x =>
            logger.info(s"delete $x")
            x match {
                case 1 => Status(204)
                case 0 => NotFound
            }
            
        }
        
    }
    

}