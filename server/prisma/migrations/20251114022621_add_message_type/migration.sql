-- CreateEnum
CREATE TYPE "message_type_enum" AS ENUM ('notification', 'chat', 'feedback');

-- AlterTable
ALTER TABLE "message" ADD COLUMN     "type" "message_type_enum" NOT NULL DEFAULT 'notification';
