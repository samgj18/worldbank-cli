/*
import cats.effect._
import cats.implicits._
import doobie._
import doobie.h2._
import doobie.implicits._
import fs2._

object H2App extends IOApp {
  override def run(args: List[String]): IO[ExitCode]     = {
    val dbLogic: ConnectionIO[List[Student]]                 =
      for {
        _   <- createTable
        _   <- Range(0, 10).toList.traverse(i =>
               insertStudent(Student(s"name-$i", i))
             )
//        res <- getStudents
        res <- getStudentsStream.compile.toList
      } yield res

    def streamLogic(xa: Transactor[IO]): Stream[IO, Student] =
      Stream.eval(createTable.transact(xa)).drain ++
        Stream
          .emits(Range(0, 10).map(i => Student(s"name-s-$i", i)))
          .evalMap(s => insertStudent(s).transact(xa).as(s))
          .evalTap(s => IO(println(s)))

    transactorResource
      .use(dbLogic.transact[IO])
      .flatMap(students => IO(println(students)))
      .as(ExitCode.Success)

    transactorResource
      .use(xa => streamLogic(xa).compile.drain)
      .as(ExitCode.Success)

    Stream
      .resource(transactorResource)
      .flatMap(streamLogic)
      .compile
      .drain
      .as(ExitCode.Success)
  }

  def transactorResource: Resource[IO, H2Transactor[IO]] =
    for {
      ce         <- ExecutionContexts.fixedThreadPool[IO](32)
      transactor <-
        H2Transactor.newH2Transactor[IO](
          url =
            "jdbc:h2:/Users/samuelgomezjimenez/Documents/GitHub/pecunia/pecunia:worldbank",
          user = "",
          pass = "",
          connectEC = ce
        )
    } yield transactor

  def createTable: ConnectionIO[Boolean]                 =
    sql"CREATE TABLE IF NOT EXISTS students(name VARCHAR(30), age INT)".update.run
      .map(_ > 0)

  def insertStudent(s: Student): ConnectionIO[Int] =
    sql"INSERT INTO students(name, age) VALUES(${s.name}, ${s.age})".update.run

  def getStudents: ConnectionIO[List[Student]] =
    sql"SELECT name, age FROM students".query[Student].to[List]

  def getStudentsStream: Stream[ConnectionIO, Student] =
    sql"SELECT name, age FROM students".query[Student].stream

  final case class Student(name: String, age: Int)

  val example =
    transactorResource
      .use { transactor =>
        val createTable =
          sql"CREATE TABLE IF NOT EXISTS students(name VARCHAR(30), age INT)".update.run
        val insert      =
          sql"INSERT INTO students(name) VALUES(1)".update.run
        val selectEx    =
          sql"SELECT COUNT(*) FROM sam_is_the_best".query[Long].unique

        val result: ConnectionIO[Long] =
          for {
            _     <- createTable
            _     <- insert
            count <- selectEx
          } yield count

        result.transact(transactor)
      }
      .flatMap(count => IO(print(count)))
}
*/
