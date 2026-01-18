# Recipe Server - Exam 2025-28a

This is the Node.js server for the Recipe Management exam.

## Setup

1. Install dependencies:
```bash
npm install
```

2. Start the server:
```bash
npm start
```

Or use the Makefile:
```bash
make
```

The server will start on port **2528**.

## API Endpoints

- `GET /recipes` - Get all recipes
- `GET /recipe/:id` - Get recipe by ID
- `POST /recipe` - Create new recipe
- `DELETE /recipe/:id` - Delete recipe by ID
- `GET /allRecipes` - Get all recipes (alternative endpoint)

## WebSocket Support

The server broadcasts new recipes to all connected WebSocket clients when a recipe is created.

## Sample Data

The server comes with 10 pre-loaded recipes including:
- Spaghetti Carbonara
- Chocolate Cake
- Caesar Salad
- Grilled Salmon
- And more...
