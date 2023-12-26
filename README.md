# resilient-backend-service



## 🚀 Run

### Backend

```bash
cd backend
./gradlew bootRun
```

> OpenAPI Specification can be found under http://localhost:8080/swagger-ui/index.html

### Frontend

> Based on [angular-movies](https://github.com/tastejs/angular-movies/tree/2fb531a51da71875bc188ebb754cacffeb080f68) from https://tastejs.com/movies/

> ⚠️ Requires [`nx`](https://nx.dev/getting-started/installation#installing-nx-globally)
>
> ```bash
> npm install --global nx@latest
> ```

```bash
cd frontend
npm install
npm run start
```



## 🧑‍💻 Architecture

Our application is underpinned by a Spring Boot backend, structured using a package-by-feature approach. This methodology ensures our architecture remains both organized and scalable, facilitating easy updates and maintenance.

The backbone of our service is the comprehensive API from "The Movie Database" (`tmdb`). We've integrated both the v3 and v4 OpenAPI Specifications (OAS) from `tmdb`, accessible for reference [here](https://developer.themoviedb.org/openapi). The integration with these APIs is streamlined through the use of springdoc, which manages our OAS/Swagger definitions.

In our architectural narrative, we play the role of a proxy, channeling all API calls from our backend to `tmdb`, with a special twist on the list feature. We recognize `tmdb` as an external API that, while generally reliable, occasionally presents challenges that require resilient solutions. Our list feature stands out by offering robust CRUD (Create, Read, Update, Delete) operations, bolstered with thorough input validation.

We currently showcase two primary features: `proxy` and `list`. The `proxy` feature acts as our gateway to `tmdb`, facilitating calls to both v3/v4 API methods and a separate endpoint for image-related interactions, acknowledging the unique requirements of this media type.

In essence, our architecture is designed not just for efficiency and reliability, but also for adaptability, ensuring a smooth and responsive experience for users and developers alike.

### *TODO: Extend in the future*
