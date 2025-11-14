-- CreateTable
CREATE TABLE "trip_feedback" (
    "id" SERIAL NOT NULL,
    "trip_id" INTEGER NOT NULL,
    "parent_id" INTEGER NOT NULL,
    "student_id" INTEGER,
    "rating" INTEGER,
    "comment" TEXT,
    "created_at" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "trip_feedback_pkey" PRIMARY KEY ("id")
);

-- CreateIndex
CREATE INDEX "trip_feedback_trip_id_parent_id_idx" ON "trip_feedback"("trip_id", "parent_id");

-- AddForeignKey
ALTER TABLE "trip_feedback" ADD CONSTRAINT "trip_feedback_trip_id_fkey" FOREIGN KEY ("trip_id") REFERENCES "trip"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "trip_feedback" ADD CONSTRAINT "trip_feedback_parent_id_fkey" FOREIGN KEY ("parent_id") REFERENCES "parents"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "trip_feedback" ADD CONSTRAINT "trip_feedback_student_id_fkey" FOREIGN KEY ("student_id") REFERENCES "students"("id") ON DELETE SET NULL ON UPDATE CASCADE;
