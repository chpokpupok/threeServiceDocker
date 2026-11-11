const { Router } = require("express");
const controller = require("../controllers/aiController");

const router = Router();

router.post("/generate-plan", controller.generatePlan);

module.exports = router;