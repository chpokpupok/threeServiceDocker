const AI_SERVICE_URL = process.env.AI_SERVICE_URL || "http://localhost:3003";

const generatePlan = async (req, res) => {
  const { topic } = req.body;

  if (!topic || !topic.trim()) {
    return res.status(422).json({ error: "Поле topic обязательно" });
  }

  try {
    const response = await fetch(`${AI_SERVICE_URL}/generate-plan`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ topic })
    });

    const data = await response.json();

    if (!response.ok) {
      return res.status(response.status).json(data);
    }

    res.json(data);
  } catch (error) {
    res.status(502).json({
      error: "AI-сервис недоступен",
      details: error.message
    });
  }
};

module.exports = { generatePlan };