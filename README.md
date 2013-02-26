Computer Security 2012
Project 2: Medical Records and Secure Connections
This project should be done in groups of 1-4 people.
1 Goal
The goal of this project is the following
1. You should practice your technical writing skills.
2. You should learn how a SSL connection between a server and a client can be set up in practice in Java,
including authentication of both the server and the client.
3. You should implement a simple access control scheme.
4. You should apply the knowledge gained in the course by identifying and documenting the strengths
and weaknesses of your solution.
2 Introduction
The purpose of this project is to study a \real case" problem that requires security mechanisms in its
solution. The project aims at going through most of the stages of the development process, including design,
implementation, evaluation and documentation. Medical records contain sensitive data and how these records
should be handled is regulated by law [3].
Looking at the law, we can see that
 Someone working at a hospital has access to a medical record only if he/she treats the patient or if
he/she needs the medical record for some other reason related to his/her work.
 A hospital can allow the patient to access his/her medical record provided that the patient is authen-
ticated.
 An audit log must be kept that logs all access to a medical record. This can help preventing, detecting
and reacting to security breaches and other types of unauthorized access to the medical records.
 Socialstyrelsen can decide that a medical record should be destroyed.
Interpreting the law, \Socialstyrelsen" provides more detailed regulations [1], see also [2]. In these documents,
among other things, we can nd that
 If an open network is used to manage medical records, the data must be transmitted such that unau-
thorized disclosure of the data is prevented, and strong authentication must be used, i.e., two factor
authentication.
 People treating a patient must have enough access so that their work can be carried out safely, but
they should not have more access than necessary. Only individual access is allowed, no group access.
 The audit log should contain information about who did what and when.
13 Project Description
You will not implement all aspects of all regulations, but the project will use them as a starting point. Let
us consider the following scenario:
The medical records in a hospital are kept in a common database managed by a server. Individuals are
allowed access to the database by remote access to the server using an open network.
Individuals are of the following types: patient, nurse, doctor, and government agency. Each patient has
one or several medical records. Each record contains the name of the associated nurse and doctor (those
who treated the patient), the hospital division (where it took place), and some medical data.
For the sake of this project, we interpret the law and the regulations as follows. Each nurse and doctor
is associated with a hospital division. Access to medical records is done according to the following rules.
 A patient is allowed to read his/her own list of records.
 A nurse may read and write to all records associated with him/her, and also read all records associated
with the same division.
 A doctor may read and write to all records associated with him/her, and also read all records associated
with the same division. In addition, the doctor can create new records for a patient provided that the
doctor is treating the patient. When creating the record, the doctor also associates a nurse with the
record.
 A government agency is allowed to read and delete all types of records.
This interpretation might seem a bit liberal, as it in some cases gives more access than allowed by the
regulations. However, this is nothing compared to reality [8]. At the same time, having access control on
medical records for hospital sta comes with problems in itself. This is a tricky subject and a reliable overall
solution does not exist. We have a tradeo between availability and condentiality and in our solution we
will have more condentiality and less availability.
Your implementation should consider a small example with a few patients, a few nurses, a few doctors,
and one government agency. Your do not need to include a database system for your records (if you don't
want to), simply store them in some convenient way. You must also make sure that all actions are properly
logged.
Access to the records is provided through a public client program (you must consider all implementations
as public knowledge in your security evaluation). The server is situated in a physically protected room.
Access to this room is only allowed under the supervision of trusted sta. Proper backup is assumed.
The project must be implemented in the Java language. You will need to write a client application,
a server application and possibly some additional application. Some form of two factor authentication of
individuals is needed and one way to accomplish this is to authenticate users with certicates. This will
require them to have both the actual keystore and also the password to the keystore. You are free to use
some other two factor authentication method if you want. All certicates must be signed by a CA, as done
in Project 1. The communication between the client and the server must be encrypted and established using
the network standard SSL. The CA certicate should be installed in a truststore on both the server and the
client. The CA private key should be used to sign all other certicates. The server should store all records
and respond to client requests. The records do not need to be stored encrypted.
4 Documentation and Presentation
Read this section very carefully. The documentation consists of two main parts, in total approximately 10
A4 pages.
 The rst part should be an overview and a technical description of SSL/TLS and certicates put into
the context of the project. In the SSL/TLS part you need only to describe the handshake as it is
2done in your program. This includes guring out which handshake method is actually negotiated. The
certicate part should relate to the certicate hierarchy used in your program. The intended audience
for this part should be a student in your program that has not taken the Computer Security course.
 The second part is a security evaluation of the design. In the security evaluation, you must enumerate
all possible and impossible attacks on the system, and for each of them summarize the protection that
your solution oers. If none (e.g., because the attack is unrealistic), this must also be stated. As only
a few examples, you should consider the following:
{ What if an attacker attempts a spoong attack, writing his/her own client, and tricks users to
use this client instead?
{ Man-in-the-middle attacks.
{ Eavesdropping communication.
{ Fake certicates.
{ The human factor. What is required by the users in order to avoid security breaches. Would it
be necessary to educate the users?
{ How should the passwords be chosen?
The documentation should not include a user guide for the program.
To be accepted on the project you have to submit the documentation and source code. The presentation
is divided into two parts, a demonstration of your program and a discussion regarding the report. All reports
must be sent with email to Paul.Stankovski@eit.lth.se. Suggest a time for presentation when sending in your
report. All group members must be present on the presentation.
There are two alternatives when demonstrating your program. Either you prepare a computer in the
basement to demonstrate your program on, or you bring your own laptop.
4.1 What You Should Think About
This is not a programming course, it is a computer security course. Your security evaluation is at least
as important as your actual program. We do not require a program that is spotless in all aspects, but
we do require that you think extensibly about all security aspects that you can nd, both technical and
nontechnical. In your program, functionality is more important than design. We will not be selling your
solution to investors, so you do not need to spend time making your program look nice. In fact, a GUI is
optional.
5 Resources
 If you do not have it already, the Java SDK package can be downloaded at [9].
 Java security documentation can be found at [4]. The most important part for you is the JSSE
documentation which can be found at [5]. This is a rather large document but you will learn tons of stu
by reading it and it makes the project much easier. Spending time reading the JSSE documentation
will probably save you lots of time in the end.
 There is sample code in the JSSE documentation. You should be most interested in ClassServer.java,
ClassFileServer.java and SSLSocketClientWithClientAuth.
 Java SE 7 API specication can be found at [7].
 The java language specication can be found at [6]
36 Hints
Here are some useful hints.
Hint 1: Before you begin coding, draw a design sketch to show which keystores and truststores your design
will be using and where. This sketch can also be used for your report.
Hint 2: Distribute the work within the group. Do not sit 4 people in front of the computer.
Hint 3: The JSSE documentation can be tough to read, but it is a very good source of information with
explanations and examples. Consulting it will save you lots of time.
Hint 4: When creating the certicates and keystores, you should take advantage of what you learned in
Project 1.
Hint 5: You will want to use the function setNeedClientAuth.
Hint 6: When you have established the connection, the server might want to read the client certicate to
check which user it is. Instead of
Socket socket;
socket = server.accept();
you could write
SSLSocket socket = (SSLSocket)theServerSocket.accept();
SSLSession session = socket.getSession();
X509Certificate cert = (X509Certificate)session.getPeerCertificateChain()[0];
String subject = cert.getSubjectDN().getName();
System.out.println (subject);
The server will now print information about the certicate. You can use this information to check the owner
of the certicate.
Hint 7: When starting a Java program, a truststore can be specied by setting the system properties
javax.net.ssl.trustStore and javax.net.ssl.trustStorePassword as
java -Djavax.net.ssl.trustStore=theTruststore -Djavax.net.ssl.trustStorePassword=passphrase
prog
but it can also be set in the source code.
Hint 8: In order to nd which handshake method is used in your program you can print the SSLSocket
object to the screen.
References
[1] SOSFS 2008:14 Informationshantering och journalforing i halso- och sjukvarden (SOSFS 2008:14)
Url: http://www.socialstyrelsen.se/sosfs/2008-14
[2] Ansvar for informationssakerhet - Styrning av behorigheter. Url: http://www.socialstyrelsen.se/
regelverk/handbocker/handbokominformationshanteringochjournalforing/25
[3] Patientdatalag (2008:355) Url: https://lagen.nu/2008:355
[4] http://download.oracle.com/javase/7/docs/technotes/guides/security/index.html
[5] http://download.oracle.com/javase/7/docs/technotes/guides/security/jsse/JSSERefGuide.
html
[6] http://java.sun.com/docs/books/jls/index.html
[7] http://download.oracle.com/javase/7/docs/api/
4[8] Datainspektionen. Bristande atkomstkontroll pa Karolinska sjukhuset, 2009-04-03.
Url: http://www.datainspektionen.se/sv/press/nyhetsarkiv/2009/
bristande-atkomstkontroll-pa-karolinska-sjukhuset/
[9] http://www.oracle.com/technetwork/java/javase/downloads/index.html
5
