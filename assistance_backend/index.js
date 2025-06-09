const express = require("express");
const mongoose = require("mongoose");
const cors = require("cors");
require("dotenv").config();

const app = express();
app.use(express.json());
app.use(cors());

// Replace with your Atlas connection string
mongoose.connect(process.env.MONGODB_URI);

const userSchema = new mongoose.Schema({
  username: String,
  password: String,
});
const User = mongoose.model("User", userSchema);

// Signup endpoint
app.post("/signup", async (req, res) => {
  const { username, password } = req.body;
  const existing = await User.findOne({ username });
  if (existing) return res.status(400).send("User already exists");
  const user = new User({ username, password });
  await user.save();
  res.send("Signup successful");
});

// Login endpoint
app.post("/login", async (req, res) => {
  const { username, password } = req.body;
  const user = await User.findOne({ username, password });
  if (user) res.send("Login successful");
  else res.status(401).send("Invalid credentials");
});

app.listen(3000, () => console.log("API running on port 3000"));
