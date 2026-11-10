const pool = require('../db.js');
const queries = require('../queries.js');

const get_faculty = async (req, res) => {
  try {
    const client = await pool.connect();
    const result = await client.query(queries.get_faculty);
    client.release();
    res.json(result.rows);
  } catch (err) {
    res.status(500).json({ error: 'Ошибка сервера', details: err.message });
  }
};

const get_faculty_by_id = async (req, res) => {
  try {
    const id = req.params.id;
    const client = await pool.connect();
    const result = await client.query(queries.get_faculty_by_id, [id]);
    client.release();
    
    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Факультет не найден' });
    }
    res.json(result.rows[0]);
  } catch (err) {
    res.status(500).json({ error: 'Ошибка сервера', details: err.message });
  }
};

const add_faculty = async (req, res) => {
  try {
    const { name, dean, phone_number } = req.body;
    const client = await pool.connect();
    const result = await client.query(queries.add_faculty, [name, dean, phone_number]);
    client.release();
    res.status(201).json(result.rows[0]);
  } catch (err) {
    res.status(500).json({ error: 'Ошибка сервера', details: err.message });
  }
};

const update_faculty = async (req, res) => {
  try {
    const id = req.params.id;
    const { name, dean, phone_number } = req.body;
    const client = await pool.connect();
    const result = await client.query(queries.update_faculty, [name, dean, phone_number, id]);
    client.release();
    
    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Факультет не найден' });
    }
    res.json(result.rows[0]);
  } catch (err) {
    res.status(500).json({ error: 'Ошибка сервера', details: err.message });
  }
};

const delete_faculty = async (req, res) => {
  try {
    const id = req.params.id;
    const client = await pool.connect();
    const result = await client.query(queries.delete_faculty, [id]);
    client.release();
    
    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Факультет не найден' });
    }
    res.json({ message: 'Факультет удален', deleted: result.rows[0] });
  } catch (err) {
    res.status(500).json({ error: 'Ошибка сервера', details: err.message });
  }
};

module.exports = {
  get_faculty,
  get_faculty_by_id,
  add_faculty,
  update_faculty,
  delete_faculty
};
