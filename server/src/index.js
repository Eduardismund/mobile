const Koa = require("koa");
const app = (module.exports = new Koa());
const server = require("http").createServer(app.callback());
const WebSocket = require("ws");
const wss = new WebSocket.Server({ server });
const Router = require("koa-router");
const cors = require("@koa/cors");
const bodyParser = require("koa-bodyparser");

app.use(bodyParser());
app.use(cors());
app.use(middleware);

function middleware(ctx, next) {
  const start = new Date();
  return next().then(() => {
    const ms = new Date() - start;
    console.log(
      `${start.toLocaleTimeString()} ${ctx.response.status} ${
        ctx.request.method
      } ${ctx.request.url} - ${ms}ms`
    );
  });
}

const courses = [
  {
    id: 1,
    name: "Mobile App Development",
    instructor: "Dr. John Smith",
    description: "Learn to build Android and iOS apps.",
    status: "ongoing",
    students: 25,
    duration: 40,
  },
  {
    id: 2,
    name: "Machine Learning Basics",
    instructor: "Prof. Alice Johnson",
    description: "Introduction to AI and ML techniques.",
    status: "upcoming",
    students: 18,
    duration: 35,
  },
  {
    id: 3,
    name: "Web Development Bootcamp",
    instructor: "Mr. Robert Brown",
    description: "Master HTML, CSS, JavaScript, and modern frameworks.",
    status: "ongoing",
    students: 30,
    duration: 50,
  },
  {
    id: 4,
    name: "Cybersecurity Fundamentals",
    instructor: "Dr. Laura White",
    description: "Learn how to protect systems from cyber threats.",
    status: "completed",
    students: 15,
    duration: 45,
  },
  {
    id: 5,
    name: "Data Structures & Algorithms",
    instructor: "Prof. Kevin Lee",
    description: "Deep dive into efficient problem-solving techniques.",
    status: "ongoing",
    students: 22,
    duration: 60,
  },
  {
    id: 6,
    name: "Cloud Computing Essentials",
    instructor: "Dr. Emily Davis",
    description: "Introduction to AWS, Azure, and cloud technologies.",
    status: "upcoming",
    students: 20,
    duration: 55,
  },
  {
    id: 7,
    name: "Blockchain & Cryptocurrency",
    instructor: "Prof. Mark Taylor",
    description: "Understand blockchain, smart contracts, and crypto.",
    status: "ongoing",
    students: 12,
    duration: 30,
  },
  {
    id: 8,
    name: "Game Development with Unity",
    instructor: "Mr. David Martinez",
    description: "Create interactive games using Unity and C#.",
    status: "ongoing",
    students: 28,
    duration: 70,
  },
  {
    id: 9,
    name: "UI/UX Design Principles",
    instructor: "Ms. Sophia Wilson",
    description: "Master user interface and user experience design.",
    status: "completed",
    students: 10,
    duration: 25,
  },
  {
    id: 10,
    name: "Big Data Analytics",
    instructor: "Dr. Richard Harris",
    description: "Learn how to process and analyze large datasets.",
    status: "upcoming",
    students: 17,
    duration: 65,
  },
];


const router = new Router();

router.get("/courses", (ctx) => {
  ctx.response.body = courses;
  ctx.response.status = 200;
});

router.get("/allCourses", (ctx) => {
  ctx.response.body = courses;
  ctx.response.status = 200;
});

router.get("/course/:id", (ctx) => {
  const { id } = ctx.params;
  const course = courses.find((c) => c.id == id);
  if (course) {
    ctx.response.body = course;
    ctx.response.status = 200;
  } else {
    ctx.response.body = { error: `Course with id ${id} not found` };
    ctx.response.status = 404;
  }
});

router.post("/course", (ctx) => {
  const { name, instructor, description, status, students, duration } =
    ctx.request.body;

  if (name && instructor && description && status && students && duration) {
    const id = courses.length > 0 ? Math.max(...courses.map((c) => c.id)) + 1 : 1;
    const newCourse = {
      id,
      name,
      instructor,
      description,
      status,
      students,
      duration,
    };
    courses.push(newCourse);

    broadcast(newCourse);
    ctx.response.body = newCourse;
    ctx.response.status = 201;
  } else {
    const errorMessage = `Missing or invalid fields name: ${name},
        instructor: ${instructor}, description: ${description}, status: ${status},
        students: ${students}, duration: ${duration}`;
    console.log(errorMessage);
    ctx.response.body = { error: errorMessage };
    ctx.response.status = 400;
  }
});

router.delete("/course/:id", (ctx) => {
  const { id } = ctx.params;
  const index = courses.findIndex((c) => c.id == id);
  if (index !== -1) {
    const deletedCourse = courses.splice(index, 1)[0];
    ctx.response.body = deletedCourse;
    ctx.response.status = 200;
  } else {
    ctx.response.body = { error: `Course with id ${id} not found` };
    ctx.response.status = 404;
  }
});

const broadcast = (data) => {
  wss.clients.forEach((client) => {
    if (client.readyState === WebSocket.OPEN) {
      client.send(JSON.stringify(data));
    }
  });
};

app.use(router.routes());
app.use(router.allowedMethods());

const port = 2506;
server.listen(port, () => {
  console.log(`Server running on port ${port}... 🚀`);
});
