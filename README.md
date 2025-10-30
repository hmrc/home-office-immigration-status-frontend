# Home Office Immigration Status Frontend

Web application providing internal HMRC staff with an interface to check customer's immigration status and rights to public funds.

## Running the tests with coverage, scalafmt and dependency checks

```bash
./run_all_tests.sh
```

## Running the app locally

    sm2 --start HOME_OFFICE_IMMIGRATION_STATUS_ALL
    sm2 --stop HOME_OFFICE_IMMIGRATION_STATUS_FRONTEND 
    sbt run

It should then be listening on port 10210

    http://localhost:10210/check-immigration-status
    
When signing in with the auth stub, the test user must have a role of `TBC`

## Shuttering

As this is an internal service shuttering is handled within the application.\
Configured by `isShuttered`.\
To shutter the service in a given environment, add `isShuttered: 'true'` to 
`app-config-ENV/home-office-immigration-status-frontend.yaml` and redeploy.


## Adding new content

When the Home Office adds new product types or immigration statuses to the values returned by their API, 
until content is added to the messages file this service will display the values as returned by the API.

#### Product Type
To add a new product type, add an entry under the `Immigration Route` header in `/conf/messages` 
where the message key is `immigration.` followed by the new product type key in lower case. 

For example, if the new product type is NEW, the message might be: `immigration.new='New Route'`

#### Immigration Status
To add a new immigration status, three entries need to be added to `/conf/messages`.

##### Previous status

A message needs adding under the `Immigration Status` header with the prefix `immigration.` followed by `EUS.` 
where the status is related to the EUS product types, or `nonEUS.` 
where the status is related to non EUS product types. Following the prefix, the immigration status should be in lower case. 

##### Header content

Then two messages need to be added under the `current statuses` header to ensure that the Status Found page headers are correct 
for the new statuses. 

These keys need the prefix `status-found.current.` followed by `EUS.` 
where the status is related to the EUS product types, or `nonEUS.` where the status is related to non EUS product types. 
Following the prefix, the immigration status should be as they are returned by the API (upper case). 

A second message key should be added the same as the first but followed by `.expired` to cover the scenario where the status has expired. 

##### Example

For example, where the new status is non-EUS and the value is NEW, three message keys should be added:
 1) previous status content: `immigration.nonEUS.new='New'`
 2) header content: `status-found.current.nonEUS.NEW=' has new status'`
 3) expired header content: `status-found.current.nonEUS.NEW.expired='’s new status has ended'`


### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
