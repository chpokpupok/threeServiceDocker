require("dotenv").config();

const express = require("express");
const swaggerUi = require("swagger-ui-express");
const swaggerJsdoc = require("swagger-jsdoc");
const crypto = require("crypto");

const app = express();
const port = process.env.PORT || 3003;

app.use(express.json());

const swaggerSpec = swaggerJsdoc({
  definition: {
    openapi: "3.0.0",
    info: {
      title: "AI Service API (GigaChat)",
      version: "1.0.0",
      description: "Сервис генерации плана курсовой на базе GigaChat (Сбер)"
    },
    // Обязательный блок для работы кнопки "Try it out"
    servers: [
      {
        url: `http://localhost:${port}`,
        description: "Локальный сервер"
      }
    ]
  },
  // Ищем комментарии в текущем файле (в server.js)
  apis: ["./server.js"]
});

app.use("/api-docs", swaggerUi.serve, swaggerUi.setup(swaggerSpec));


// --- Логика работы с GigaChat ---

let cachedToken = null;
let tokenExpiresAt = 0;

async function getGigaChatToken() {
  if (cachedToken && Date.now() < tokenExpiresAt - 60 * 1000) {
    return cachedToken;
  }

  const authKey = process.env.GIGACHAT_AUTH_KEY;
  const scope = process.env.GIGACHAT_SCOPE || "GIGACHAT_API_PERS";

  if (!authKey) {
    throw new Error("GIGACHAT_AUTH_KEY не задан в .env");
  }

  const rquid = crypto.randomUUID();

  const params = new URLSearchParams();
  params.append("scope", scope);

  const response = await fetch("https://ngw.devices.sberbank.ru:9443/api/v2/oauth", {
    method: "POST",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded",
      "Accept": "application/json",
      "RqUID": rquid,
      "Authorization": `Basic ${authKey}`
    },
    body: params.toString()
  });

  const data = await response.json();

  if (!response.ok) {
    throw new Error(`Ошибка получения токена GigaChat: ${JSON.stringify(data)}`);
  }

  cachedToken = data.access_token;
  tokenExpiresAt = data.expires_at;
  return cachedToken;
}

async function callGigaChat(topic) {
  const token = await getGigaChatToken();
  const model = process.env.GIGACHAT_MODEL || "GigaChat";

  const prompt = `Составь краткий план курсовой работы из 3 глав и заключения по теме: "${topic}". Верни только структуру плана, без лишних пояснений.`;

  const response = await fetch("https://gigachat.devices.sberbank.ru/api/v1/chat/completions", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Accept": "application/json",
      "Authorization": `Bearer ${token}`
    },
    body: JSON.stringify({
      model: model,
      messages: [
        { role: "user", content: prompt }
      ],
      temperature: 0.7,
      max_tokens: 800
    })
  });

  const data = await response.json();

  if (!response.ok) {
    throw new Error(`Ошибка запроса к GigaChat: ${JSON.stringify(data)}`);
  }

  const answer = data.choices?.[0]?.message?.content;
  if (!answer) {
    throw new Error("Не удалось извлечь ответ из GigaChat");
  }

  return { plan: answer };
}

/**
 * @swagger
 * tags:
 *   name: AI
 *   description: Генерация текста с помощью GigaChat
 */

/**
 * @swagger
 * /generate-plan:
 *   post:
 *     summary: Сгенерировать план курсовой (GigaChat)
 *     tags: [AI]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required:
 *               - topic
 *             properties:
 *               topic:
 *                 type: string
 *                 description: Тема курсовой работы
 *                 example: "Разработка веб-приложения на Node.js"
 *     responses:
 *       200:
 *         description: План успешно сгенерирован
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 plan:
 *                   type: string
 *                   description: Текст плана от нейросети
 *       422:
 *         description: Ошибка валидации (поле topic пустое)
 *       502:
 *         description: Ошибка на стороне GigaChat API (неверный токен, нет сети и т.д.)
 */

app.post("/generate-plan", async (req, res) => {
  const { topic } = req.body;

  if (!topic || !topic.trim()) {
    return res.status(422).json({ error: "Поле topic обязательно" });
  }

  try {
    const result = await callGigaChat(topic);
    res.json(result);
  } catch (error) {
    console.error("Ошибка GigaChat:", error);
    res.status(502).json({
      error: "Ошибка GigaChat API",
      details: error.message
    });
  }
});

app.get("/", (req, res) => {
  res.send("AI service (GigaChat) is running");
});

app.listen(port, () => {
  console.log(`AI service (GigaChat): http://localhost:${port}`);
  console.log(`Swagger UI: http://localhost:${port}/api-docs`);
});