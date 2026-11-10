require("dotenv").config();

const express = require("express");
const cookieParser = require("cookie-parser");
const swaggerUi = require("swagger-ui-express");
const swaggerJsdoc = require("swagger-jsdoc");

const queryRoutes = require("./routes/queryRouter");
const procedureRoutes = require("./routes/procedureRouter");
const functionRoutes = require("./routes/functionRouter");
const aiRoutes = require("./routes/aiRouter");

const app = express();
const port = process.env.PORT || 3002;

app.use(express.json());
app.use(cookieParser());

const swaggerSpec = swaggerJsdoc({
  definition: {
    openapi: "3.0.0",
    info: {
      title: "Coursework Service API",
      version: "1.0.0",
      description: "Сервис курсовых работ"
    }
  },
  apis: ["./routes/*.js"]
});

app.use("/api-docs", swaggerUi.serve, swaggerUi.setup(swaggerSpec));

app.get("/", (req, res) => {
  res.send("Coursework service is running");
});

app.use("/api/query", queryRoutes);
app.use("/api/procedure", procedureRoutes);
app.use("/api/function", functionRoutes);
app.use("/api/ai", aiRoutes);

app.listen(port, () => {
  console.log(`Coursework service: http://localhost:${port}`);
});