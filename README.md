# Pocket Sight

Pocket Sight is an Android app to help you manage your personal finances. The app allows users to 
fully create and edit accounts, categories, subcategories, transactions, transfers and 
recurring transactions. The app also has a *stats* page to help you keep on track with your 
spending.
<p align="center">
  <img src="https://github.com/Rodrigo-Duarte-8128/pocket-sight/blob/master/app-image.jpg" width="250"/>
</p>

## Installation
If you want to install the app on your Android devices you can simply download the file 
**app-debug.apk** and run it on your device. 


## About

The following points are useful to keep in mind while using the app:
- A **transaction** is either an expense or an income.
- A **transfer** is a change of value between accounts.
- A transfer can include accounts which are not tracked by the app. In this case, the 
app refers to these as *Another*.
- An **act** is a transaction or a transfer.
- The **main** account is the default account shown in the home screen when no other
account has been displayed in the past.
- A **recurring act** is an act that happens every month. Recurring acts have
a **month day** attribute which represents the day in which the acts occurs.
- Recurring acts have a **start date**, which can be selected from the *more options* 
menu option.
- Recurring acts are only instantiated on app start-up.

## The Stats Page

In the stats page you can see a table with four entries. The *recurring in*, *recurring out*, 
*total in* and *total out* amounts. 
- **Recurring In**: this refers to the total amount of recurring income which 
you will receive in the displayed month.
- **Recurring Out**: this refers to the total amount of recurring expenses which 
you will spend in the displayed month.
- **Total In**: this refers to the total amount received so far in the 
displayed month plus the amount to receive from recurring income in the remainder of 
the month.
- **Total Out**: this refers to the total amount spent so far in the
displayed month plus the amount to spend from recurring expenses in the remainder of
the month.

You will also find information regarding the *initial budget* and the *current budget*.
- The **initial budget** is equal to the recurring in minus the recurring out values.
- The **current budget** is equal to the total in minus the total out values.

The **progress bar** displays the current budget as a fraction of the initial budget. The most
important information on this page is the value of the current budget, since this is what determines
whether or not you are spending more than what you receive during this month.



