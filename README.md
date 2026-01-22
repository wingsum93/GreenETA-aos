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
The path to get single hospital waiting time
https://www.ha.org.hk/aedwt/index.html?Lang=chien&AEHospital=XXX 
where XXX can be replace as the short code of hospital, ie, QEH, KWH ...... and the data are as follow
```json
{
  "hospName": "Queen Elizabeth Hospital",
  "t1wt": "0 minute",
  "manageT1case": "N",
  "t2wt": "less than 15 minutes",
  "manageT2case": "Y",
  "t3p50": "14 minutes",
  "t3p95": "34 minutes",
  "t45p50": "1.5 hours",
  "t45p95": "3.5 hours",
  "updateTime": "23/1/2026 5:45AM"
}
```

| Field Name     | 描述                      | Type | Length | 備註                                                             |
| -------------- | ----------------------- | ---- | ------ | -------------------------------------------------------------- |
| `hospName`     | 醫院名稱                    | Text | 60     | 例子：瑪嘉烈醫院                                                       |
| `t1wt`         | 分流類別 I（危殆）病人的預計急症室等候時間  | Text | 10     | - 多名病人正在搶救中；或<br>- 病人到達急症室求診預計等候時間（分鐘）                         |
| `manageT1case` | 急症室是否正在治理分流類別 I（危殆）的病人  | Text | 3      | **允許值：**<br>- 不適用<br>- 是<br>- 否<br>**註釋：** 不適用是指急症室正有多名病人正在搶救中 |
| `t2wt`         | 分流類別 II（危急）病人的預計急症室等候時間 | Text | 10     | - 多名病人正在搶救中；或<br>- 病人到達急症室求診預計等候時間（分鐘）                         |
| `manageT2case` | 急症室是否正在治理分流類別 II（危急）的病人 | Text | 3      | **允許值：**<br>- 不適用<br>- 是<br>- 否<br>**註釋：** 不適用是指急症室正有多名病人正在搶救中 |
| Field Name | 描述                                         | Type | Length | 備註                  |
| ---------- | ------------------------------------------ | ---- | ------ | ------------------- |
| `t3p50`    | 分流類別 III（緊急）病人的預計急症室等候時間；一半輪候中的病人能在該時間內就診  | Text | 10     | 病人到達急症室求診預計等候時間（分鐘） |
| `t3p95`    | 分流類別 III（緊急）病人的預計急症室等候時間；大部份輪候中的病人能在該時間內就診 | Text | 10     | 病人到達急症室求診預計等候時間（分鐘） |
| Field Name | 描述                                                 | Type | Length | 備註                                        |
| ---------- | -------------------------------------------------- | ---- | ------ | ----------------------------------------- |
| `t45p50`   | 分流類別 IV 及 V（次緊急及非緊急）病人的預計急症室等候時間；一半輪候中的病人能在該時間內就診  | Text | 10     | 病人到達急症室求診預計等候時間（小時）<br>（向上捨入至最接近的 0.5 小時） |
| `t45p95`   | 分流類別 IV 及 V（次緊急及非緊急）病人的預計急症室等候時間；大部份輪候中的病人能在該時間內就診 | Text | 10     | 病人到達急症室求診預計等候時間（小時）<br>（向上捨入至最接近的 0.5 小時） |

For the full hospital list, see hkhospital.geojson file