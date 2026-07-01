# Huong dan cai dat va cau hinh Swagger

Tai lieu nay mo ta tung buoc cai dat Swagger cho project Spring Boot nay, bao gom ca phan security va cach xu ly loi `swagger-initializer.js`.

## 1. Them dependency

Mo file [pom.xml](D:/clothiq/clothiq-backend/pom.xml) va them dependency:

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.17</version>
</dependency>
```

Dependency nay cung cap:

- OpenAPI JSON: `/v3/api-docs`
- Swagger config: `/v3/api-docs/swagger-config`
- Swagger UI: `/swagger-ui/index.html`

## 2. Mo route Swagger trong Spring Security

Mo file [WebSecurityConfig.java](D:/clothiq/clothiq-backend/src/main/java/commerce/sbEcommerce/security/WebSecurityConfig.java) va them cac route Swagger vao `permitAll()`:

```java
new AntPathRequestMatcher("/swagger-ui/**"),
new AntPathRequestMatcher("/webjars/swagger-ui/**"),
new AntPathRequestMatcher("/swagger-ui.html"),
new AntPathRequestMatcher("/v3/api-docs/**")
```

Neu thieu `/webjars/swagger-ui/**` thi khi vao truc tiep URL webjar se de bi `401`.

## 3. Cau hinh resource handler cho Swagger UI

Project nay dang dung file [SwaggerUiResourceConfig.java](D:/clothiq/clothiq-backend/src/main/java/commerce/sbEcommerce/config/SwaggerUiResourceConfig.java) de override `SwaggerWebMvcConfigurer` cua `springdoc`.

Muc dich:

- tranh loi mapping pattern khong hop le voi Spring Boot 3.4
- dang ky lai resource handler cho Swagger UI
- map dung cac duong dan:
  - `/swagger-ui/**`
  - `/webjars/swagger-ui/**`

Neu project gap loi:

```text
Invalid mapping pattern detected:
/swagger-ui/**/*swagger-initializer.js
```

thi day la phan can duoc override.

## 4. Tao endpoint rieng cho swagger-initializer.js

Project nay co them file [SwaggerUiInitializerController.java](D:/clothiq/clothiq-backend/src/main/java/commerce/sbEcommerce/config/SwaggerUiInitializerController.java).

File nay tra truc tiep JavaScript cho 2 URL:

- `/swagger-ui/swagger-initializer.js`
- `/webjars/swagger-ui/swagger-initializer.js`

Muc dich:

- fix loi `404` cua `swagger-initializer.js`
- fix loi browser:

```text
Refused to execute script ... because its MIME type ('application/json') is not executable
```

Neu khong co controller nay, `index.html` co the load duoc nhung Swagger UI van hong do file initializer tra ve JSON loi thay vi JavaScript.

## 5. Cau hinh application.yml

Mo file [application.yml](D:/clothiq/clothiq-backend/src/main/resources/application.yml).

Project hien tai dang co:

```yml
spring:
  mvc:
    pathmatch:
      matching-strategy: ant_path_matcher
```

Muc nay da co san trong project. Tuy nhien voi cau hinh Swagger hien tai, chi rely vao dong nay la khong du. Van can resource config va initializer controller nhu tren.

## 6. Build lai project

Chay:

```powershell
mvn -q -DskipTests compile
```

Neu compile thanh cong, cau hinh Swagger hop le ve mat source code.

## 7. Restart backend

Sau moi thay doi lien quan den Swagger hoac Security, phai restart backend.

Neu dang co process cu chiem cong `8080`, co the dung:

```powershell
Stop-Process -Id <PID>
```

Sau do chay lai app.

## 8. Kiem tra cac endpoint

Sau khi app chay lai, kiem tra lan luot:

```text
http://localhost:8080/v3/api-docs
http://localhost:8080/v3/api-docs/swagger-config
http://localhost:8080/swagger-ui/index.html
http://localhost:8080/swagger-ui/swagger-initializer.js
```

Co the test them:

```text
http://localhost:8080/webjars/swagger-ui/index.html
http://localhost:8080/webjars/swagger-ui/swagger-initializer.js
```

## 9. Cach nhan biet loi thuong gap

### Loi 401

Nguyen nhan thuong la chua whitelist route Swagger trong `WebSecurityConfig`.

Can kiem tra:

- `/swagger-ui/**`
- `/webjars/swagger-ui/**`
- `/v3/api-docs/**`

### Loi 404 voi swagger-initializer.js

Nguyen nhan thuong la resource mapping cua `springdoc` khong resolve dung file JS.

Cach fix trong project nay:

- dung `SwaggerUiResourceConfig`
- dung `SwaggerUiInitializerController`

### Loi MIME type la application/json

Day la he qua cua loi `404`. Browser dang nhan JSON error response thay vi file JavaScript thuc te.

## 10. Trang Swagger nen dung

URL uu tien:

```text
http://localhost:8080/swagger-ui/index.html
```

Khong nen uu tien vao URL webjar tru khi can debug static resources:

```text
http://localhost:8080/webjars/swagger-ui/index.html
```

## 11. Tom tat file lien quan trong project

- [pom.xml](D:/clothiq/clothiq-backend/pom.xml)
- [application.yml](D:/clothiq/clothiq-backend/src/main/resources/application.yml)
- [WebSecurityConfig.java](D:/clothiq/clothiq-backend/src/main/java/commerce/sbEcommerce/security/WebSecurityConfig.java)
- [SwaggerUiResourceConfig.java](D:/clothiq/clothiq-backend/src/main/java/commerce/sbEcommerce/config/SwaggerUiResourceConfig.java)
- [SwaggerUiInitializerController.java](D:/clothiq/clothiq-backend/src/main/java/commerce/sbEcommerce/config/SwaggerUiInitializerController.java)

## 12. Lenh kiem tra nhanh

```powershell
mvn -q -DskipTests compile
Invoke-WebRequest -UseBasicParsing http://localhost:8080/swagger-ui/index.html
Invoke-WebRequest -UseBasicParsing http://localhost:8080/swagger-ui/swagger-initializer.js
Invoke-WebRequest -UseBasicParsing http://localhost:8080/v3/api-docs/swagger-config
```

Neu 3 endpoint tren tra ve dung, Swagger cua project nay da hoat dong on.
