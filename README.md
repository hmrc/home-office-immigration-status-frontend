# Home Office Immigration Status Frontend

Web application providing internal HMRC staff with an interface to check customer's immigration status and rights to public funds.

## Running the tests

    sbt test it:test

## Running the tests with coverage

    sbt clean coverageOn test it:test coverageReport

## Running the app locally

    sm --start HOME_OFFICE_IMMIGRATION_STATUS_ALL -r
    sm --stop HOME_OFFICE_IMMIGRATION_STATUS_FRONTEND 
    sbt run

It should then be listening on port 10210

    browse http://localhost:10210/check-immigration-status

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
