const { Router } = require("express");
const controller = require("../controllers/aiController");

const router = Router();

/**
 * @swagger
 * /api/ai/generate-plan:
 *   post:
 *     summary: Сгенерировать план курсовой по теме
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               topic:
 *                 type: string
 *     responses:
 *       200:
 *         description: План сгенерирован
 */
router.post("/generate-plan", controller.generatePlan);

module.exports = router;