require("dotenv").config();

const express = require("express");
const cookieParser = require("cookie-parser");
const swaggerUi = require("swagger-ui-express");
const swaggerJsdoc = require("swagger-jsdoc");

const userRoutes = require("./routes/userRouter");

const app = express();
const port = process.env.PORT || 3001;

app.use(express.json());
app.use(cookieParser());

const swaggerSpec = swaggerJsdoc({
  definition: {
    openapi: "3.0.0",
    info: {
      title: "Auth Service API",
      version: "1.0.0",
      description: "Сервис авторизации и регистрации"
    }
  },
  apis: ["./routes/*.js"]
});

app.use("/api-docs", swaggerUi.serve, swaggerUi.setup(swaggerSpec));

app.get("/", (req, res) => {
  res.send("Auth service is running");
});

app.use("/home", userRoutes);

app.listen(port, () => {
  console.log(`Auth service: http://localhost:${port}`);
});