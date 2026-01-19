# GreenETA
GreenETA is a convenient app that help user to find the last hospital with current waiting time for different categories injuries.

## Tech stack
- MVVM architecture
- Network client library, Ktor-android.
- Jetpack compose UI
- Kotlin
- Runtime permission

### 患者傷勢分類 in hk
What is the 患者傷勢分類 classification of hk hospital?

## Feature of the app
- find the top 2 closed hospital and the 

急症室等候時間
https://data.gov.hk/tc-data/dataset/hospital-hadata-ae-waiting-time

### Sample json return from hospital API
url for all hospital waiting time (en)
This url show all hospital but it's not useful as it do not support grouping into 3 area.
https://www.ha.org.hk/opendata/aed/aedwtdata2-en.json
```json
{"waitTime":[{"hospName":"Alice Ho Miu Ling Nethersole Hospital","t1wt":"0 minute","manageT1case":"N","t2wt":"less than 15 minutes","manageT2case":"N","t3p50":"28 minutes","t3p95":"77 minutes","t45p50":"3 hours","t45p95":"4 hours"},{"hospName":"Caritas Medical Centre","t1wt":"0 minute","manageT1case":"N","t2wt":"less than 15 minutes","manageT2case":"N","t3p50":"25 minutes","t3p95":"54 minutes","t45p50":"3 hours","t45p95":"4 hours"},{"hospName":"Kwong Wah Hospital","t1wt":"0 minute","manageT1case":"N","t2wt":"less than 15 minutes","manageT2case":"N","t3p50":"29 minutes","t3p95":"82 minutes","t45p50":"3.5 hours","t45p95":"4.5 hours"},{"hospName":"North District Hospital","t1wt":"0 minute","manageT1case":"N","t2wt":"less than 15 minutes","manageT2case":"N","t3p50":"20 minutes","t3p95":"57 minutes","t45p50":"1 hour","t45p95":"3 hours"},{"hospName":"North Lantau Hospital","t1wt":"0 minute","manageT1case":"N","t2wt":"less than 15 minutes","manageT2case":"N","t3p50":"14 minutes","t3p95":"35 minutes","t45p50":"0.5 hours","t45p95":"1 hour"},{"hospName":"Pamela Youde Nethersole Eastern Hospital","t1wt":"0 minute","manageT1case":"N","t2wt":"less than 15 minutes","manageT2case":"N","t3p50":"32 minutes","t3p95":"62 minutes","t45p50":"4.5 hours","t45p95":"7.5 hours"},{"hospName":"Pok Oi Hospital","t1wt":"0 minute","manageT1case":"N","t2wt":"less than 15 minutes","manageT2case":"N","t3p50":"16 minutes","t3p95":"31 minutes","t45p50":"3 hours","t45p95":"3.5 hours"},{"hospName":"Prince of Wales Hospital","t1wt":"0 minute","manageT1case":"N","t2wt":"less than 15 minutes","manageT2case":"N","t3p50":"23 minutes","t3p95":"89 minutes","t45p50":"4 hours","t45p95":"6 hours"},{"hospName":"Princess Margaret Hospital","t1wt":"0 minute","manageT1case":"N","t2wt":"less than 15 minutes","manageT2case":"N","t3p50":"21 minutes","t3p95":"51 minutes","t45p50":"2 hours","t45p95":"4 hours"},{"hospName":"Queen Elizabeth Hospital","t1wt":"0 minute","manageT1case":"N","t2wt":"less than 15 minutes","manageT2case":"Y","t3p50":"20 minutes","t3p95":"41 minutes","t45p50":"3 hours","t45p95":"4.5 hours"},{"hospName":"Queen Mary Hospital","t1wt":"0 minute","manageT1case":"N","t2wt":"less than 15 minutes","manageT2case":"N","t3p50":"33 minutes","t3p95":"78 minutes","t45p50":"4.5 hours","t45p95":"5 hours"},{"hospName":"Ruttonjee Hospital","t1wt":"0 minute","manageT1case":"N","t2wt":"less than 15 minutes","manageT2case":"N","t3p50":"15 minutes","t3p95":"43 minutes","t45p50":"1 hour","t45p95":"2.5 hours"},{"hospName":"St John Hospital","t1wt":"0 minute","manageT1case":"N","t2wt":"less than 15 minutes","manageT2case":"N","t3p50":"0 minute","t3p95":"0 minute","t45p50":"0 hour","t45p95":"0 hour"},{"hospName":"Tin Shui Wai Hospital","t1wt":"0 minute","manageT1case":"N","t2wt":"less than 15 minutes","manageT2case":"N","t3p50":"12 minutes","t3p95":"25 minutes","t45p50":"2.5 hours","t45p95":"3.5 hours"},{"hospName":"Tseung Kwan O Hospital","t1wt":"0 minute","manageT1case":"N","t2wt":"less than 15 minutes","manageT2case":"N","t3p50":"15 minutes","t3p95":"37 minutes","t45p50":"4 hours","t45p95":"7 hours"},{"hospName":"Tuen Mun Hospital","t1wt":"0 minute","manageT1case":"N","t2wt":"less than 15 minutes","manageT2case":"Y","t3p50":"20 minutes","t3p95":"37 minutes","t45p50":"3.5 hours","t45p95":"4.5 hours"},{"hospName":"United Christian Hospital","t1wt":"0 minute","manageT1case":"Y","t2wt":"less than 15 minutes","manageT2case":"Y","t3p50":"28 minutes","t3p95":"77 minutes","t45p50":"4.5 hours","t45p95":"7 hours"},{"hospName":"Yan Chai Hospital","t1wt":"0 minute","manageT1case":"N","t2wt":"less than 15 minutes","manageT2case":"N","t3p50":"24 minutes","t3p95":"65 minutes","t45p50":"3 hours","t45p95":"5 hours"}],"updateTime":"20/1/2026 4:30AM"}
```
