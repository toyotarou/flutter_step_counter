import 'dart:async';
import 'package:flutter/material.dart';
import 'pigeon/step_api.dart'; // ← Pigeonで生成されたファイル

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return const MaterialApp(home: StepRecordListPage());
  }
}

class StepRecordListPage extends StatefulWidget {
  const StepRecordListPage({super.key});

  @override
  State<StepRecordListPage> createState() => _StepRecordListPageState();
}

class _StepRecordListPageState extends State<StepRecordListPage> {
  List<StepRecord?> _records = [];
  Timer? _timer;

  @override
  void initState() {
    super.initState();
    _loadRecords();
    _timer = Timer.periodic(const Duration(seconds: 5), (_) => _loadRecords());
  }

  Future<void> _loadRecords() async {
    final records = await StepApi().getAllStepRecords();
    setState(() {
      _records = records;
    });
  }

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('歩数記録一覧')),
      body: ListView.builder(
        itemCount: _records.length,
        itemBuilder: (context, index) {
          final record = _records[index];

          if (record == null) {
            return SizedBox.shrink();
          }

          return ListTile(
            leading: const Icon(Icons.directions_walk),
            title: Text("📅 ${record.date} (区間${record.segment})"),
            subtitle: Text("🕒 ${record.time} - 👣 ${record.step}歩"),
          );
        },
      ),
    );
  }
}
