# team-two Design Document

## *Project Title* Design

## 1. Problem Statement

CSV Files are commonly used to store, or transfer information. Spreadsheet programs are the most common way to easily open, edit, and export csv files. Once the size of a csv file increases beyond a certain point, it can be slow, or even impossible to use spreadsheet programs to open, or edit csv files.

This design document describes the "help, my csv is too big" service, which will allow used to upload csv files, view some of its data such as headers, or the number of rows. The service will allow users to filter out columns they don't need, and reduce the amount of information in the file. The service will then return to the user a csv file with only the information they requested.


## 2. Top Questions to Resolve in Review

*List the most important questions you have about your design, or things that
you are still debating internally that you might like help working through.*

1. Will S3 be the only service required to query the file, or will I need another service to query the file once it's been uploaded?
2. 
3.  

## 3. Use Cases

*This is where we work backwards from the customer and define what our customers
would like to do (and why). You may also include use cases for yourselves, or
for the organization providing the product to customers.*

U1. *As a user, I want to be able to upload a csv to the service. I also want to be able to specify what delimiter my file is using.*

U2. *As a user, I want to be able to view the general information about the file I just uploaded (e.g. Row Count, Column Count, Size)*
    
U3. *As a user, I want to be able to select what columns I want.*

U4. *As a user, I want to be able to see general information about specific columns (e.g. for a number column, min, median, max aggregations).*

U5. *As a user, I want to be able to create filters on columns for row-level data that limits the number of rows that are returned (e.g. 'Only show rows where the value of the column field "Color" is "Red"').*

U6. *As a user, I want to be able to download a csv with only the data I selected.*

U7. *As a user, I want to be able to view the files I've previously uploaded, and the filters I've used to download files in the past.*

## 4. Project Scope

### 4.1. In Scope

Parts 1,2,3,6

### 4.2. Out of Scope

Parts 4,5,7

# 5. Proposed Architecture Overview

*Describe broadly how you are proposing to solve for the requirements you
described in Section 3.*

*This may include class diagram(s) showing what components you are planning to
build.*

*You should argue why this architecture (organization of components) is
reasonable. That is, why it represents a good data flow and a good separation of
concerns. Where applicable, argue why this architecture satisfies the stated
requirements.*

# 6. API

## 6.1. Public Models

*Define the data models your service will expose in its responses via your
*`-Model`* package. These will be equivalent to the *`PlaylistModel`* and
*`SongModel`* from the Unit 3 project.*

## 6.2. *First Endpoint*

*Describe the behavior of the first endpoint you will build into your service
API. This should include what data it requires, what data it returns, and how it
will handle any known failure cases. You should also include a sequence diagram
showing how a user interaction goes from user to website to service to database,
and back. This first endpoint can serve as a template for subsequent endpoints.
(If there is a significant difference on a subsequent endpoint, review that with
your team before building it!)*

*(You should have a separate section for each of the endpoints you are expecting
to build...)*

## 6.3 *Second Endpoint*

*(repeat, but you can use shorthand here, indicating what is different, likely
primarily the data in/out and error conditions. If the sequence diagram is
nearly identical, you can say in a few words how it is the same/different from
the first endpoint)*

# 7. Tables

*Define the DynamoDB tables you will need for the data your service will use. It
may be helpful to first think of what objects your service will need, then
translate that to a table structure, like with the *`Playlist` POJO* versus the
`playlists` table in the Unit 3 project.*

# 8. Pages

*Include mock-ups of the web pages you expect to build. These can be as
sophisticated as mockups/wireframes using drawing software, or as simple as
hand-drawn pictures that represent the key customer-facing components of the
pages. It should be clear what the interactions will be on the page, especially
where customers enter and submit data. You may want to accompany the mockups
with some description of behaviors of the page (e.g. “When customer submits the
submit-dog-photo button, the customer is sent to the doggie detail page”)*
